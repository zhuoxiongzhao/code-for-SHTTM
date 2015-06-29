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
int AValue = 32;
int BValue = 16;
int ALPHA = 32;
int GAMMA = 32;
int LAMDA = 32;
int ITERATION_TIME = 1;

// to be modified.
Mat T, C, Y, U, W, Theta;
Mat M;
SpMat X;
int numOfDoc = 11269;
int numOfTag = 6;
int numOfCod = 6;
int numOfDem = 53975;

void obtainYandU ()
{
	Y = Theta;
	Mat I = Mat::Identity(numOfCod, numOfCod);
	for (int _=1;_<=ITERATION_TIME;++_)
	{
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
			Mat CI = Mat::Identity(numOfTag, numOfTag);
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
	Mat w0 = Y*(X.transpose());
	SpMat I(numOfDem, numOfDem);
	vector<Trip> v;
	for (int i = 0; i < numOfDem; ++ i) v.push_back(Trip(i,i,1));
	I.setFromTriplets(v.begin(), v.end());
	SpMat w1 = (X*(X.transpose())).pruned(0, 1) + LAMDA*I;
	// W = w0 * (w1)^-1
}

void obtainMedian ()
{
	M=Mat(numOfDoc, 1);
	for (int i = 0; i < numOfDoc; ++ i)
	{
		vector<double> v;
		for (int j = 0; j < numOfCod; ++ j)
			v.push_back(Y(j, i));
		sort(v.begin(), v.end());
		M(i, 0) = (v[numOfCod/2] + v[numOfCod-1-numOfCod/2])/2;
	}
}

Mat convertY (Mat Y)
{
	Mat YH = Mat::Zero(Y.rows(), Y.cols());
	for (int j = 0; j < Y.cols(); ++ j)
	{
		for (int i = 0; i < Y.rows(); ++ i)
		{
			if ( Y(i, j) > M(j) ) YH(i, j) = 1;
			else YH(i, j) = -1;
		}
	}
	return YH;
}

int main ()
{
	puts("The program consists of 7 steps!");
	puts("Step 1: Get LDA Value and Init Data Start");
	puts("Step 1: Get LDA Value and Init Data Finish");
		
	// Step 2: Construct Confidence Matrix C
	puts("Step 2: Construct Confidence Matrix C Start");
	Mat C = T*(BValue-AValue) + Mat::Ones(T.rows(), T.cols())*AValue;
	puts("Step 2: Construct Confidence Matrix C Finish");
	
	// Step 3: Calculate Y and U
	puts("Step 3: Calculate Y and U Start");
	obtainYandU();
	puts("Step 3: Calculate Y and U Finish");

	
	// Step 4: Calculate W
	puts("Step 4: Calculate W Start");
	obtainW();
	puts("Step 4: Calculate W Finish");
	
	// Step 5: Calculate Median Vector
	puts("Step 5: Calculate Median Vector Start");
	obtainMedian();
	puts("Step 5: Calculate Median Vector Finish");
	
	// Step 6: Calculate the hashcode Matrix YH
	puts("Step 6: Calculate hashcode Matrix YH Start");
	Mat YH = convertY(Y);
	puts("Step 6: Calculate hashcode Matrix YH Finish");
	
	// Step 7: test answer!
	puts("Step 7: Testing start!");
	
	puts("Step 7: Testing Finish!");
	return 0;
}
