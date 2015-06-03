%% for newsgroup database

clear all, close all,clc;
data = dlmread('train.data');
test = dlmread('test.data');
train_gnd = dlmread('train.label');
test_gnd = dlmread('test.label');

cal_train_vector = zeros(max(data(:,1)), 1);
for i = 1:size(data, 1)
    cal_train_vector( data(i,1) ) = cal_train_vector( data(i,1) ) + 1;
end

for i = 1:size(cal_train_vector, 1)
%    if cal_train_vector(i) == 0
        disp(i);
%    end
end


cal_test_vector = zeros(max(test(:,1)), 1);
for i = 1:size(test, 1)
    cal_test_vector( test(i,1) ) = cal_test_vector( test(i,1) ) + 1;
end

for i = 1:size(cal_test_vector, 1)
    if cal_test_vector(i) == 0
        disp(i);
    end
end

% cal the num of document 
cal_num = zeros(1281, 1);
for i = 1:size(DS, 2)
    
    cal_num(DS(i)) = cal_num(DS(i)) + 1;
end

%% test for the LDA tool box

load 'bagofwords_psychreview'; 
% Load the psych review vocabulary
load 'words_psychreview'; 
% Set the number of topics
T=50; 
% Set the hyperparameters
BETA=0.01;
ALPHA=50/T;
% The number of iterations
N = 300; 
% The random seed
SEED = 3;
% What output to show (0=no output; 1=iterations; 2=all output)
OUTPUT = 1;
load('ldasingle_psychreview.mat');

% Put the most 7 likely words per topic in cell structure S
[S] = WriteTopics( WP , BETA , WO , 7 , 0.7 );

fprintf( '\n\nMost likely words in the first ten topics:\n' );

% Show the most likely words in the first ten topics
S( 1:10 )  

% Write the topics to a text file
WriteTopics( WP , BETA , WO , 10 , 0.7 , 4 , 'topics.txt' );

fprintf( '\n\nInspect the file ''topics.txt'' for a text-based summary of the topics\n' ); 


topic_cal = zeros( 50, 1 );
for i = 1:85031
    if DS(i) == 1281
        topic_cal(Z(i)) = topic_cal(Z(i)) + 1;
        disp(WO{WS(i),1});
    end
end



%% test for the newgroup by topictoolbox
T=50; 
% Set the hyperparameters
BETA=0.01;
ALPHA=50/T;
% The number of iterations
N = 300; 
% The random seed
SEED = 3;
% What output to show (0=no output; 1=iterations; 2=all output)
OUTPUT = 1;


[ WS , DS ] = importworddoccounts( 'test.data' );
[ WO ] = textread( 'vocabulary.txt' , '%s' );
[ WP,DP,Z ] = GibbsSamplerLDA( WS,DS,T,N,ALPHA,BETA,SEED,OUTPUT );
save('new_train_lda.mat','WS','DS','WO','WP','DP','Z');

load('new_train_lda.mat');




topic_cal = zeros( 6, 1 );
for i = 1:size(Z,2)
    if DS(i) == 1
        topic_cal(Z(i)) = topic_cal(Z(i)) + 1;
        disp(WO{WS(i),1});
    end
end

test_gnd = load('test.label');
gnd = test_gnd;
% to map the label into 6 classes
map_vector = [6,1,1,1,1,1,4,2,2,2,2,3,3,3,3,6,5,5,5,5];
for i = 1:size(test_gnd,1)
    gnd(i) = map_vector(gnd(i));
end

% the lda label cluster
lda_label = zeros(size(gnd,1),1);
for i = 1:size(lda_label,1)
    lda_label(i) = find(DP(i,:)==max(DP(i,:)),1);
end

% to sum up the number of different class docs
sum_lda_doc = zeros(6,1);
for i = 1:size(lda_label,1)
    sum_lda_doc(lda_label(i)) = sum_lda_doc(lda_label(i))+1;
end

sum_ori_doc = zeros(6,1);
for i = 1:size(gnd,1)
    sum_ori_doc(gnd(i)) = sum_ori_doc(gnd(i))+1;
end

% correspond
% sum_ori_doc = [1945;1588;1573;382;1301;716]
% sum_lda_doc = [754;908;1614;1903;1486;840]







%% the fellowing codes are for STHHM
test_data = load('test.data');



num_of_bit = 6;     % num_of_bit = k in the paper
% L = 6;   ==class_num       % the possible tags of each document;  the real tag nunmber

% the data after LDA
% Set the hyperparameters
BETA=0.01;
ALPHA=50/num_of_bit;
% The number of iterations
N = 300; 
% The random seed
SEED = 3;
% What output to show (0=no output; 1=iterations; 2=all output)
OUTPUT = 1;
% [ WS , DS ] = importworddoccounts( 'test.data' );
% [ WO ] = textread( 'vocabulary.txt' , '%s' );
% [ WP,DP,Z ] = GibbsSamplerLDA( WS,DS,num_of_bit,N,ALPHA,BETA,SEED,OUTPUT );
% save('new_train_lda.mat','WS','DS','WO','WP','DP','Z');

load('new_train_lda.mat');


% fea_X = sparse( size(WO,1), size(gnd, 1) );
% % fea_X = zeros( size(WO,1), size(gnd, 1) );
% for sample_i = 1:size(test_data,1)
%     sample_i
%     fea_X(test_data(sample_i,2),test_data(sample_i,1)) = test_data(sample_i,3);
% end
% save('test_fea_X.mat','fea_X');

load('test_fea_X.mat');


test_gnd = load('test.label');
gnd = test_gnd;
class_num = 6;
% to map the label into 6 classes
map_vector = [6,1,1,1,1,1,4,2,2,2,2,3,3,3,3,6,5,5,5,5];
for i = 1:size(test_gnd,1)
    gnd(i) = map_vector(gnd(i));
end

nor_DP = sparse(size(DP,1),size(DP,2));
for i = 1:size(nor_DP,1)
    for j = 1:size(nor_DP,2)
        nor_DP(i,j) = DP(i,j)/sum(DP(i,:));
    end
end

xita = nor_DP';

tag_T = zeros( class_num, size(gnd,1) ); %T(l*n)
for i = 1:size(gnd,1)
    tag_T(gnd(i),i) = 1;
end
confidence_matrix_C = zeros(size(tag_T,1),size(tag_T,2));   % have problem in paper for the dimension of confidence matrix C

c_map_vector = [ 0.01, 1 ];
for c = 1:class_num
    for j = 1:size(gnd,1)
        confidence_matrix_C( c, j ) = c_map_vector( tag_T( c, j )+1 );
    end
end

% initialization
Y_hashing_code = xita;
alpha = 1;   % maybe [ 0.5, 1, 2, 4, 8, 16, 32, 128 ]
gamma = 1;
lamda = 1;
latent_variable_U = zeros( num_of_bit, class_num );     % k*l

% iteration

for iter = 1:10;
    iteridentify_eye = sparse(size(fea_X,1));
for i = 1:size(fea_X,1)
    identify_eye(i,i) = 1;
end
parameter_matrix_W = Y_hashing_code*fea_X'*inv(fea_X*fea_X'+lamda*identify_eye);
Y_hashing_code_project = parameter_matrix_W*fea_X;
m_median_vector = median( Y_hashing_code_project,2 );


    % for U
    for i = 1:class_num
        diagonal_matrix_C = diag(confidence_matrix_C(i,:));
        T_i = tag_T( i, : )';
        latent_variable_U( :, i ) = inv(Y_hashing_code*diagonal_matrix_C*Y_hashing_code'+alpha*eye(num_of_bit))*Y_hashing_code*diagonal_matrix_C*T_i;
    end
    % for Y
    for j = 1:size( gnd, 1 )
        Y_hashing_code( :, j ) = inv(latent_variable_U*diag(confidence_matrix_C( :, j ))*latent_variable_U'+gamma*eye(num_of_bit))*(latent_variable_U*diag(confidence_matrix_C( :, j ))*tag_T(:,j)+gamma+xita(:,j));
    end
end

save('train_w.mat','Y_hashing_code','latent_variable_U');
load('train_w.mat');

disp('cal identify_eye');
identify_eye = sparse(size(fea_X,1));
for i = 1:size(fea_X,1)
    identify_eye(i,i) = 1;
end
disp('cal W');



tic;
% parameter_matrix_W = Y_hashing_code*fea_X'*inv(fea_X*fea_X'+lamda*identify_eye);

parameter_matrix_W = Y_hashing_code*fea_X'/(fea_X*fea_X'+lamda*identify_eye);
toc;


disp('cal Y_hashing_code_project');
Y_hashing_code_project = parameter_matrix_W*fea_X;
disp('cal m_median_vector');
m_median_vector = median( Y_hashing_code_project,2 );
save('train_result.mat','Y_hashing_code_project','parameter_matrix_W','m_median_vector');

load('train_result.mat');
Y_binary_code_test = Y_hashing_code_project>=repmat(m_median_vector,[1,size(Y_hashing_code_project,2)]);
Y_binary_code_test = Y_binary_code_test*2-1;


train_data = load('train.data');
train_gnd = load('train.label');

fea_X_train = sparse( size(WO,1), size(train_gnd, 1) );

for sample_i = 1:size(train_data,1)
    sample_i
    fea_X_train(train_data(sample_i,2),train_data(sample_i,1)) = train_data(sample_i,3);
end
save('fea_X_train.mat','fea_X_train');
load('fea_X_train.mat');


% for test

Y_fea_train = parameter_matrix_W*fea_X_train;
Y_binary_code_train = Y_fea_train>=repmat(m_median_vector,[1,size(Y_fea_train,2)]);
Y_binary_code_train = Y_binary_code_train*2-1;

distance_matrix = zeros(size(Y_binary_code_test,2),size(Y_binary_code_train,2));
for i = 1:size(Y_binary_code_test,2)
    i
    % for j = 1:size(Y_binary_code_train,2)
    for j =1:10
        distance_matrix(i,j) = pdist2(Y_binary_code_test(:,i)',Y_binary_code_train(:,j)','hamming');
    end
end

[ ~, rank_index ] = sort(distance_matrix,1);



result_gnd = zeros(size(Y_binary_code_train,2),2);
result_gnd(:,2) = train_gnd;

for j = 1:10
    cal_vector = zeros( class_num, 1 );
    for i = 1:size(rank_index,1)
        if rank_index(i)<=100
            cal_vector(gnd(i)) = cal_vector(gnd(i))+1;
        end
    end
    result_gnd(j,1) = find(cal_vector==max(cal_vector));
end





