%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This is the code for SHTTM of Matlab version.
% Power by Zhuoxiong Zhao @ SCUT
% E-mail: zhuoxiong.zhao@gmail.com
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%



%% load data
test_data = load('test.data');
train_data = load('train.data');
train_gnd = load('train.label');
test_gnd = load('test.label');


%% label mapping, to map the label into 6 classes
map_vector = [6,1,1,1,1,1,4,2,2,2,2,3,3,3,3,6,5,5,5,5];
for i = 1:size(test_gnd,1)
    test_gnd(i) = map_vector(test_gnd(i));
end

for i = 1:size(train_gnd,1)
    train_gnd(i) = map_vector(train_gnd(i));
end

class_num = 6;
num_of_bit = 6;     % num_of_bit = k in the paper, the bit of binary codes of hashing codes

%% 
disp('LDA process');
% Set the hyperparameters for LDA
BETA=0.01;
ALPHA=50/num_of_bit;
% The number of iterations
N = 300; 
% The random seed
SEED = 3;
% What output to show (0=no output; 1=iterations; 2=all output)
OUTPUT = 1;
[ WS , DS ] = importworddoccounts( 'train.data' );
[ WO ] = textread( 'vocabulary.txt' , '%s' );
[ WP,DP,Z ] = GibbsSamplerLDA( WS,DS,num_of_bit,N,ALPHA,BETA,SEED,OUTPUT );
save('LDA_train.mat','WS','DS','WO','WP','DP','Z');
% load('LDA_train.mat');

%% xita normalization
nor_DP = sparse(size(DP,1),size(DP,2));
for i = 1:size(nor_DP,1)
    for j = 1:size(nor_DP,2)
        nor_DP(i,j) = DP(i,j)/sum(DP(i,:));
    end
end
xita = nor_DP';


%% train & test fea
disp('convert into sparse fea');
fea_X_train = spalloc( size(WO,1), size(train_gnd, 1), size(train_data,1) );
for sample_i = 1:size(train_data,1)
    fea_X_train(train_data(sample_i,2),train_data(sample_i,1)) = train_data(sample_i,3);
end

fea_X_test = spalloc( size(WO,1), size(test_gnd, 1),size(test_data,1) );
for sample_i = 1:size(test_data,1)
    fea_X_test(test_data(sample_i,2),test_data(sample_i,1)) = test_data(sample_i,3);
end
save('fea_X.mat','fea_X_test', 'fea_X_train');
%load('fea_X.mat');


%% tag for training data
tag_T = zeros( class_num, size(train_gnd,1) ); %T(l*n)
for i = 1:size(train_gnd,1)
    tag_T(train_gnd(i),i) = 1;
end


%% confidence matrix
confidence_matrix_C = zeros(size(tag_T,1),size(tag_T,2));   % have problem in paper for the dimension of confidence matrix C
c_map_vector = [ 0.01, 1 ];
for c = 1:class_num
    for j = 1:size(train_gnd,1)
        confidence_matrix_C( c, j ) = c_map_vector( tag_T( c, j )+1 );
    end
end


%% initialization for Y_hashing_code
Y_hashing_code = xita;
alpha = 1;   % maybe [ 0.5, 1, 2, 4, 8, 16, 32, 128 ]
gamma = 1;
lamda = 1;
latent_variable_U = zeros( num_of_bit, class_num );     % k*l


%% iteration process to obtain optimal Y and U
for iter = 1:10;
    iter
    % for U
    for i = 1:class_num
        diagonal_matrix_C = diag(confidence_matrix_C(i,:));
        T_i = tag_T( i, : )';
        latent_variable_U( :, i ) = (Y_hashing_code*diagonal_matrix_C*Y_hashing_code'+alpha*eye(num_of_bit))\Y_hashing_code*diagonal_matrix_C*T_i;
    end
    % for Y
    for j = 1:size( train_gnd, 1 )
        Y_hashing_code( :, j ) = (latent_variable_U*diag(confidence_matrix_C( :, j ))*latent_variable_U'+gamma*eye(num_of_bit))\(latent_variable_U*diag(confidence_matrix_C( :, j ))*tag_T(:,j)+gamma+xita(:,j));
    end
end


%% to cal parameter metrix W
disp('cal parameter metrix W');
identify_eye = spalloc( size(fea_X_train,1), size(fea_X_train,1), size(fea_X_train,1) );
for i = 1:size(fea_X_train,1)
    identify_eye(i,i) = 1;
end

tic;
parameter_matrix_W = Y_hashing_code*fea_X_train'/(fea_X_train*fea_X_train'+lamda*identify_eye);
toc;

%% projection
disp('cal Y_hashing_code_project');
Y_project_train = parameter_matrix_W*fea_X_train;
m_median_vector = median( Y_project_train, 2 );
Y_binary_code_train = Y_project_train>=repmat(m_median_vector,[1,size(Y_project_train,2)]);
Y_binary_code_train = Y_binary_code_train*2-1;

save('train_result.mat', 'Y_project_train', 'Y_binary_code_train', 'parameter_matrix_W', 'm_median_vector');
% load('train_result.mat');


%% for test
disp('testing');
Y_project_test = parameter_matrix_W*fea_X_test;
Y_binary_code_test = Y_project_test>=repmat(m_median_vector,[1,size(Y_project_test,2)]);
Y_binary_code_test = Y_binary_code_test*2-1;

distance_matrix = zeros(size(Y_binary_code_train,2),size(Y_binary_code_test,2));
for i = 1:size( Y_binary_code_train, 2 )
    i
    % for j = 1:size(Y_binary_code_train,2)
    for j =1:size( Y_binary_code_test, 2 )
        distance_matrix(i,j) = pdist2(Y_binary_code_train(:,i)',Y_binary_code_test(:,j)','hamming');
    end
end

[ ~, rank_index ] = sort(distance_matrix,1);


result_gnd = zeros(size(Y_binary_code_test,2),2);
result_gnd(:,2) = test_gnd;

for j = 1:10
    cal_vector = zeros( class_num, 1 );
    for i = 1:size(rank_index,1)
        if rank_index(i)<=100
            cal_vector(test_gnd(i)) = cal_vector(test_gnd(i))+1;
        end
    end
    result_gnd(j,1) = find(cal_vector==max(cal_vector),1);
end


acc_num = 0;
for i = 1:size( test_gnd, 1 )
    if result_gnd(i,1) == result_gnd(i,2)
        acc_num = acc_num+1;
    end
end
accuracy = acc_num/size( test_gnd, 1 );





