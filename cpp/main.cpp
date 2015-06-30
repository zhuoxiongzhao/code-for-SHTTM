/**
 * Author: Jun Hu
 * Date: 2015-06-27
 * Description: SHTTM Implememtation
 */ 

#include <cstdio>
#include <ctime>
#include <iostream>
#include <algorithm>
#include <Eigen/Dense>
#include <Eigen/Sparse>
#include <vector>
#include <fstream>
using namespace std;
using namespace Eigen;

// Define Short name of class!
typedef SparseMatrix<double> SpMat;
typedef SparseVector<double> SpVec;
typedef SpMat::InnerIterator SpMatIt;
typedef MatrixXd Mat;
typedef Triplet<double> Trip;

// debug use!
void Shape (const Mat &a)
{
	cout<<a.rows()<<" "<<a.cols()<<endl;
}

// Initial Data
double AValue = 0.01;
double BValue = 1;
double ALPHA = 1;
double GAMMA = 1;
double LAMDA = 1;
int ITERATION_TIME = 10;
int map_vector[] = {5,0,0,0,0,0,3,1,1,1,1,2,2,2,2,5,4,4,4,4};

// to be modified.
Mat T, C, Y, U, W, Theta;
Mat M;
SpMat X;
const int numOfDoc = 11269;
const int numOfTag = 6;
const int numOfCod = 6;
const int numOfDem = 61188;

void obtainYandU ()
{
	Y = Theta;
	U = Mat::Zero(numOfCod, numOfTag);
	Mat I = Mat::Identity(numOfCod, numOfCod);
	for (int _=1;_<=ITERATION_TIME;++_)
	{
		cout << "Iter time " << _ << endl;
		// for U
		for (int i = 0; i < numOfTag; ++ i)
		{
			SpMat CI(numOfDoc, numOfDoc);
			vector<Trip> v;
			for (int j = 0; j < numOfDoc; ++ j) 
				v.push_back(Trip(j,j,C(i,j)));
			CI.setFromTriplets(v.begin(), v.end());
			Mat TI(numOfDoc, 1);
			for (int j = 0; j < numOfDoc; ++ j)
				TI(j, 0) = T(i, j);
			U.col(i)=(Y*CI*(Y.transpose())+ALPHA*I).inverse()*Y*CI*TI;
		}
		// for Y
		for (int i = 0; i < numOfDoc; ++ i)
		{
			Mat CI = Mat::Zero(numOfTag, numOfTag);
			for (int j = 0; j < numOfTag; ++ j)
				CI(j, j) = C(j, i);
			
			Mat TI(numOfTag, 1);
			for (int j = 0; j < numOfTag; ++ j)
				TI(j, 0) = T(j, i);
			
			Y.col(i)=(U*CI*(U.transpose())+GAMMA*I).inverse()*(U*CI*TI+GAMMA*Theta.col(i));
		}
	}
}

void obtainW ()
{
	Mat w0 = X*(Y.transpose());
	SpMat I(numOfDem, numOfDem);
	vector<Trip> v;
	for (int i = 0; i < numOfDem; ++ i) v.push_back(Trip(i,i,1));
	I.setFromTriplets(v.begin(), v.end());
	
	// pruned number  
	SpMat w1 = ((X*(X.transpose())).pruned(1) + LAMDA*I).transpose();
	
	
	printf("X*XT finished\n");
	
	//w1 * W' =  w0
	SparseQR<SpMat, COLAMDOrdering<int> > linearSolver;
	linearSolver.compute(w1);
	W = Mat::Zero(numOfCod, numOfDem);
	for (int i = 0; i < numOfCod; ++ i)
	{
		printf("num %d\n", i);
		
		VectorXd vec(numOfDem);
		for (int j = 0; j < numOfDem; ++ j)
			vec[j] = w0(j, i);
		W.row(i)=linearSolver.solve(vec);
	}
}

void obtainMedian ()
{
	M=Mat(numOfDoc, 1);
	for (int i = 0; i < numOfCod; ++ i)
	{
		vector<double> v;
		for (int j = 0; j < numOfDoc; ++ j)
			v.push_back(Y(i, j));
		sort(v.begin(), v.end());
		M(i, 0) = (v[numOfDoc/2] + v[numOfDoc-1-numOfDoc/2])/2;
	}
}

Mat convertY (Mat Y)
{
	Mat YH = Mat::Zero(Y.rows(), Y.cols());
	for (int j = 0; j < Y.cols(); ++ j)
	{
		for (int i = 0; i < Y.rows(); ++ i)
		{
			if ( Y(i, j) > M(i, 0) ) YH(i, j) = 1;
			else YH(i, j) = -1;
		}
	}
	return YH;
}

void formX (SpMat &X, int num, const char* path) {
	X = SpMat(numOfDem, num);
	vector<Trip> vec; 
	fstream in(path);
	int i, j;double k;
	while (in>>j>>i>>k)
		vec.push_back(Trip(i-1,j-1,k));
	X.setFromTriplets(vec.begin(), vec.end());
	in.close();
}

void formT (Mat &T, int num, const char* path) {
	T = Mat::Zero(numOfTag, num);
	fstream in(path);
	int i, id = 0;
	while (in>>i)
		T(map_vector[i-1], id++) = 1;
	in.close();
}

void inputMat (Mat &Q, const char* path) {
	fstream in(path);
	int numI, numJ;
	in >> numI >> numJ;
	Q = Mat::Zero(numI, numJ);
	for (int i = 0; i < numI; ++ i)
		for (int j = 0; j < numJ; ++ j)
			in >> Q(i, j);
	in.close();
}

void outputMat (Mat &Q, const char* path) {
	ofstream out(path);
	out<<Q.rows()<<" "<<Q.cols()<<endl;
	for (int i = 0; i < Q.rows(); ++ i) {
		for (int j = 0; j < Q.cols(); ++ j)
			out << Q(i, j) << " ";
		out << endl;
	}
	out.close();
}

void init ()
{
	formX(X, numOfDoc, "./train.data");
	formT(T, numOfDoc, "./train.label");
	inputMat(Theta, "./theta.data");
}

int main ()
{
	int st = clock ();
	puts("The program consists of 6 steps!");
	puts("Step 1: Get LDA Value and Init Data Start");
	init ();
	printf("Step 1: Get LDA Value and Init Data Finish %dms\n", clock()-st);
		
	// Step 2: Construct Confidence Matrix C
	puts("Step 2: Construct Confidence Matrix C Start");
	C = T*(BValue-AValue) + Mat::Ones(T.rows(), T.cols())*AValue;
	printf("Step 2: Construct Confidence Matrix C Finish %dms\n", clock()-st);

	// Step 3: Calculate Y and U
	puts("Step 3: Calculate Y and U Start");
	obtainYandU();
	printf("Step 3: Calculate Y and U Finish %dms\n", clock()-st);

	
	// Step 4: Calculate W
	puts("Step 4: Calculate W Start");
	obtainW();
	printf("Step 4: Calculate W Finish %dms\n", clock()-st);
	
	// Step 5: Calculate Median Vector
	puts("Step 5: Calculate Median Vector Start");
	obtainMedian();
	printf("Step 5: Calculate Median Vector Finish %dms\n", clock()-st);
	
	// Step 6: Calculate the hashcode Matrix YH
	puts("Step 6: Calculate hashcode Matrix YH Start");
	Mat YH = convertY(Y);
	printf("Step 6: Calculate hashcode Matrix YH Finish %dms\n", clock()-st);
	
	outputMat(W, "W.data");
	outputMat(M, "M.data");
	outputMat(YH, "YH.data");
	outputMat(U, "U.data");
	return 0;
}
