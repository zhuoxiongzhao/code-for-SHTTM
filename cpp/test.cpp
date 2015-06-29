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
double AValue = 1;
double BValue = 0.01;
double ALPHA = 1;
double GAMMA = 1;
double LAMDA = 1;
int ITERATION_TIME = 10;
int map_vector[] = {5,0,0,0,0,0,3,1,1,1,1,2,2,2,2,5,4,4,4,4};

// to be modified.
Mat T, C, Y, U, W, Theta;
Mat M;
SpMat X;

//YH train YH2 test
Mat YH, YH2;

int numOfDocTrain = 11269;
int numOfDocTest = 7505;
int numOfTag = 6;
int numOfCod = 6;
int numOfDem = 61188;

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

void formGnd (Mat &G, int num, const char* path) {
	G = Mat::Zero(num, 1);
	fstream in(path);
	int i, id = 0;
	while (in>>i)
		G(id++) = map_vector[i-1];
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

Mat G_train, G_test;

void init ()
{
	formX(X, numOfDocTest, "./test.data");
	formT(T, numOfDocTest, "./test.label");
	formGnd(G_train, numOfDocTrain, "./train.label");
	formGnd(G_test, numOfDocTest, "./test.label");
	inputMat(W, "W.data");
	inputMat(M, "M.data");
	inputMat(YH, "YH.data");
}

int main ()
{
	Mat P = Mat::Identity(3,3);
	outputMat(P, "p.data");
	
	puts("Testing start!");
	init ();
	Y = W * X;
	YH2 = convertY(Y);
	Mat dis = Mat::Zero(YH.cols(), YH2.cols());
	// hamming distance
	for (int i = 0; i < YH.cols(); ++ i)
	{
		for (int j = 0; j < YH2.cols(); ++ j)
		{
			for (int k = 0; k < YH.rows(); ++ k)
			{
				if (YH(k, i) != YH2(k, j)) dis(i, j) ++;
			}
		}
	}
	
	int acc_num = 0;
	for (int j = 0; j < YH2.cols(); ++ j)
	{
		vector<pair<double, int> > vs;
		for (int i = 0; i < YH.cols(); ++ i)
			vs.push_back(make_pair(dis(i, j), G_train(i)));
		sort(vs.begin(), vs.end());
		
		Mat cnt = Mat::Zero(numOfTag, 1);
		for (int i = 0; i < 100; ++ i) 
			cnt(vs[i].second, 0) ++;
		
		int id = 0;
		for (int i = 0; i < numOfTag; ++ i) if (cnt(i, 0) > cnt(id, 0))
			id = i;
			
		if (id == G_test(j)) acc_num ++;
	}
	printf("ACC NUM IS %d\n", acc_num);
	printf("ACC Percent is %.6f\n", acc_num*1. / YH2.cols());
	puts("Testing Finish!");
	return 0;
}
