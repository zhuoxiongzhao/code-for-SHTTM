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

clock_t st;

void caltime ()
{
	printf("Time pass: %lu\n", (clock()-st) / CLOCKS_PER_SEC);
}

// Initial Data
int map_vector[] = {5,0,0,0,0,0,3,1,1,1,1,2,2,2,2,5,4,4,4,4};

// to be modified.
Mat T, Y, W;
Mat M;
SpMat X;

//YH train YH2 test
Mat YH, YH2;

const int numOfDocTrain = 11269;
const int numOfDocTest = 7505;
const int numOfTag = 6;
const int numOfDem = 61188;

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

void formGnd (int G[], const char* path) {
	fstream in(path);
	int i, id = 0;
	while (in>>i)
		G[id++] = map_vector[i-1];
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

int G_train[numOfDocTrain], G_test[numOfDocTest];

void init ()
{
	st = clock();
	formX(X, numOfDocTest, "./test.data");
	formT(T, numOfDocTest, "./test.label");
	formGnd(G_train, "./train.label");
	formGnd(G_test, "./test.label");
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

	// hamming dis and acc num
	int acc_num = 0;
	for (int i = 0; i < YH2.cols(); ++ i)
	{
		vector<pair<int, int> > vs;
		for (int j = 0; j < YH.cols(); ++ j)
		{
			int dis = 0;
			for (int k = 0; k < YH.rows(); ++ k)
				if (YH2(k, i) != YH(k, j)) dis ++;
			vs.push_back(make_pair(dis, G_train[j]));
		}
		sort(vs.begin(), vs.end());
		int cnt[numOfTag] = {0}, id = 0;
		for (int i = 0; i < 100; ++ i)
			cnt[ vs[i].second ] ++;
		for (int i = 0; i < numOfTag; ++ i) if (cnt[i] > cnt[id])
			id = i;
		
		if (id == G_test[i]) acc_num++;
	}
	
	printf("ACC NUM IS %d\n", acc_num);
	printf("ACC Percent is %.6f\n", acc_num*1. / numOfDocTest);
	puts("Testing Finish!");
	return 0;
}
