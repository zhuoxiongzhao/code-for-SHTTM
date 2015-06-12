#!/usr/bin/env python
# -*- coding: utf-8 -*-

__author__ = 'xiexiaochao'

import numpy as np
import lda
import functools
import time

# 用于跟踪函数执行的时间及进度
def log(func) :
	@functools.wraps(func)
	def wrapper(*args, **kw) :
		print 'begin call : %s' %func.__name__
		startTime = time.time()
		result = func(*args, **kw)
		endTime = time.time()
		print 'end call : %s' %func.__name__
		print 'duration : ', endTime - startTime
		print '======================================================================================'
		return result
	return wrapper

# 初始给出的数据
AValue = 32
BValue = 16
ALPHA = 32
GAMMA = 32
LAMTA = 32
ITERATION_TIME = 1

# 构造LDA分布 theta
# @params X - 一个m*n的np.array矩阵，n是文档个数，m是词数
# @return doc_topic - 每个文档的前n_topics的分布
@log
def getLda(X):
	# n_topics: number of topics
	# n_iter: number of sampling iterations
	# random_state: the generator used for the initial topics
	model = lda.LDA(n_topics = 6, n_iter = 10, random_state = 1)

	model.fit(X.transpose())  # model.fit_transform(X) is also available

	# the document-topic distributions 
	doc_topic = model.doc_topic_
	return doc_topic

# 构造置信矩阵 C l * n
# @params T - 标签矩阵
# @params numOfDoc - 文档个数
# @params numOfTag - 标签个数
# @return C - 置信矩阵 C
@log
def initMatrixC(T, numOfDoc, numOfTag):
	C = np.zeros((numOfTag, numOfDoc))
	for i in range(0, numOfTag):
		for j in range(0, numOfDoc):
			if T[i][j] == 1:
				C[i][j] = AValue
			else:
				C[i][j] = BValue
	return C

# 初始化哈希矩阵 Y
# @params theta - 文档的lda分布矩阵
# @return Y - 哈希矩阵 
@log
def initY(theta):
	Y = theta
	return Y

# 初始化单位矩阵 I
# @params numOfTopK - 单位矩阵大小
# @return I - 单位矩阵
def initI(num):
	I = np.identity(num)
	return I

# 初始化迭代矩阵CI,用于计算矩阵U
# @params rowNumOfTag - 标签行下表
# @params numOfDoc -文档个数
# @param matrixC - 置信矩阵
# @return CI - 迭代矩阵
def initCI(rowNumOfTag, numOfDoc, matrixC):
	CI = np.zeros((numOfDoc, numOfDoc))
	for i in range(0, numOfDoc):
		for j in range(0, numOfDoc):
			if i == j:
				CI[i][j] = matrixC[rowNumOfTag][i]
			else :
				CI[i][j] = 0.0
	return CI

# 初始化迭代矩阵TI,用于计算矩阵U
# @params rowNumOfTag - 标签行下表
# @params numOfDoc -文档个数
# @params numOfTag -标签个数
# @param matrixT - 标签矩阵
# @return TI - 迭代矩阵
def initTI(rowNumOfTag, numOfTag, numOfDoc, matrixT):
	TI = np.zeros((numOfDoc, numOfTag))
	for i in range(0, numOfDoc):
		for j in range(0, numOfTag) :
			TI[i][0] = matrixT[rowNumOfTag][i]
	return TI

# 迭代计算隐藏变量 U
# @params numOfTag - 标签个数
# @params numOfDoc - 文档个数
# @params numOfCode - 哈希码长度
# @params matrixY - 哈希矩阵 Y
# @params matrixI - 单位矩阵 I
# @params matrixC - 置信矩阵 C
# @params matrixT - 标签矩阵 T
# @return U - 隐藏矩阵 U
def obtainU(numOfTag, numOfDoc, numOfCode, matrixY, matrixI, matrixC, matrixT):
	for i in range(0, numOfTag):
		CI = initCI(i, numOfDoc, matrixC)
		UI = np.dot(np.dot(matrixY, CI), matrixY.transpose()) + ALPHA * matrixI
		UI = np.asarray(np.asmatrix(UI).I)
		UI = np.dot(np.dot(np.dot(UI, matrixY), CI), matrixT.transpose())

	return UI

# 初始化迭代矩阵CJ,用于计算矩阵Y
# @params colNumOfDoc - 文档列
# @params numOfTag -标签个数
# @param matrixC - 置信矩阵
# @return CJ - 迭代矩阵
def initCJ(colNumOfDoc, numOfTag, matrixC):
	CJ = np.zeros((numOfTag, numOfTag))
	for i in range(0, numOfTag):
		for j in range(0, numOfTag):
			if i == j:
				CJ[i][j] = matrixC[j][colNumOfDoc]
			else :
				CJ[i][j] = 0.0
	return CJ

# 初始化迭代矩阵TJ,用于计算矩阵Y
# @params colNumOfDoc - 文档列
# @params numOfTag -标签个数
# @param matrixT - 标签矩阵
# @return TJ - 迭代矩阵
def initTJ(colNumOfDoc, numOfTag, matrixT):
	TJ = np.zeros((numOfTag, 1))
	for i in range(0, numOfTag):
		TJ[i][0] = matrixT[i][colNumOfDoc]
	return TJ

# 迭代计算哈希矩阵 Y
# @params numOfTag - 标签个数
# @params numOfDoc - 文档个数
# @params numOfCode - 哈希码长度
# @params matrixU - 隐藏矩阵 U
# @params matrixI - 单位矩阵 I
# @params matrixC - 置信矩阵 C
# @params matrixT - 标签矩阵 T
# @params theta - 分布矩阵 theta
# @return Y - 哈希矩阵 Y
def obtainY(numOfTag, numOfDoc, numOfCode, matrixU, matrixI, matrixC, matrixT, theta):
	for i in range(0, numOfDoc):
		CJ = initCJ(i, numOfTag, matrixC)
		# TJ = initTI(i, numOfTag, matrixT)
		YI1 = np.dot(np.dot(matrixU, CJ), matrixU.transpose()) + GAMMA * matrixI
		YI1 = np.asarray(np.asmatrix(YI1).I)
		YI2 = np.dot(np.dot(matrixU, CJ), matrixT) + GAMMA * theta
		YI = np.dot(YI1, YI2)

	return YI

# 迭代计算哈希矩阵 Y
# @params numOfTag - 标签个数
# @params numOfDoc - 文档个数
# @params numOfCode - 哈希码长度
# @params matrixY - 初始哈希矩阵 Y
# @params matrixC - 置信矩阵 C
# @params matrixT - 标签矩阵 T
# @params theta - 分布矩阵 theta
# @return U - 隐藏矩阵 U
# @return matrixY - 哈希矩阵 Y
@log
def coordinateDescent(numOfTag, numOfDoc, numOfCode, matrixY, matrixC, matrixT, theta):
	I = initI(numOfCode)
	U = 1
	for i in range(ITERATION_TIME):
		U = obtainU(numOfTag, numOfDoc, numOfCode, matrixY, I, matrixC, matrixT)
		matrixY = obtainY(numOfTag, numOfDoc, numOfCode, U, I, matrixC, matrixT, theta)
	return U, matrixY

# 计算哈希函数矩阵 W
# @params numOfDimen - 文档维度
# @params numOfCode - 哈希码长度
# @params matrixY - 哈希矩阵 Y
# @params matrixX - 文档矩阵 X
# @params matrixI - 单位矩阵 I
# @return W - 哈希函数矩阵 W
@log
def obtainW(numOfDimen, numOfCode, matrixY, matrixX, matrixI):
	# W = np.zeros((numOfDimen, numOfCode))
	W1 = np.dot(matrixY, matrixX.transpose())
	W2 = np.dot(matrixX, matrixX.transpose()) + LAMTA * matrixI
	W0 = np.dot(W1, np.asarray(np.asmatrix(W2).I))
	# for i in range(0, numOfDimen):
	# 	for j in range(0, numOfCode):
	# 		W[i][j] = W0[j][i]
	return W0

# 计算中间向量 M
# @params numOfDoc - 文档个数
# @params numOfCode - 哈希码长度
# @params matrixY - 哈希矩阵 Y
# @return M - 中间向量 M
@log
def obtainMedianM(numOfDoc, numOfCode, matrixY):
	M = np.zeros((numOfDoc, 1))
	for i in range(0, numOfDoc):
		subMatrix = matrixY.transpose()[i]
		subMatrix.sort()
		if(numOfCode % 2 == 0):
			M[i] = (subMatrix[numOfCode / 2 -1] + subMatrix[numOfCode / 2]) / 2.0
		else :
			M[i] = subMatrix[numOfCode / 2]
	return M

# 通过medianM计算 Y
# @params numOfDoc - 文档个数
# @params numOfCode - 哈希码长度
# @params matrixY - 哈希矩阵 Y
# @params medianM - 中间向量 M
# @return matrixY - 哈希矩阵 Y
@log
def obtainYViaThresholdMatrixY(numOfDoc, numOfCode, matrixY, medianM):
	for i in range(0, numOfDoc) :
		for j in range(0, numOfCode) :
			if matrixY[j][i] > medianM[i]:
				matrixY[j][i] = -1
			else :
				matrixY[j][i] = 1
	return matrixY

# 查找矩阵的维度
# @params filePath - 文件路劲
# @return maxNum - 最大维度
def findDemin(filePath):
	fp = open(filePath, "r")
	maxNum = 0
	for eachline in fp:
		subs = eachline.split(' ')
		if int(subs[1]) > maxNum:
			maxNum = int(subs[1])
	return maxNum

def initX(filePath, numOfDoc) :
	# 查找文档最大维度
	X = np.zeros((findDemin(filePath), numOfDoc), dtype = 'int64')
	fp = open(filePath, "r")
	for eachline in fp:
		subs = eachline.split(' ')
		X[int(subs[1]) - 1][int(subs[0]) - 1] = int(subs[2])
	return X

# 初始化输入矩阵 X
# m * n - n = 11269
@log
def initMatrixX(filePath, numOfDoc):
	X = initX(filePath, numOfDoc)
	return X

# 初始化标签矩阵 T
# l * n - l = 6， = 111269
@log
def initMatrixT(filePath, numOfDoc, numOfTag):
	index = 0
	docId = 0
	T = np.zeros((numOfTag, numOfDoc), dtype = 'float64')
	map_vector = [6,1,1,1,1,1,4,2,2,2,2,3,3,3,3,6,5,5,5,5]
	fp = open(filePath, "r")
	for eachline in fp:
		labelId = int(eachline)
		T[map_vector[labelId - 1] - 1][docId] = 1.0
		docId += 1
	return T

if __name__ == '__main__':
	# 初始化数据
	numOfDoc = 11269
	numOfTag = 6
	numOfCode = 6
	numOfDimen = 53975

	X = initMatrixX('./train.data', numOfDoc)
	print X.shape
	T = initMatrixT('./train.label', numOfDoc, numOfTag)

	# 步骤一，获取文档分布theta
	# 这个后面记得修改 LDA 的迭代次数
	# theta = getLda(X).transpose()
	# print theta.shape

	# 由于计算 theta 的时间开销比较大，把第一次计算后的矩阵存入文件
	# 后面直接读取，方便测试
	# theta.tofile("./train.bin")
	 
	# 读取存在文件中的 theta
	theta = np.fromfile("./train.bin", dtype = np.float64)
	theta.shape = 6, 11269

	# 步骤二，构造置信矩阵
	C = initMatrixC(T, numOfDoc, numOfTag)
	
	print C
	print C.shape

	# 步骤三，初始化 Y
	Y = initY(theta)
	print Y.shape

	# 步骤四，坐标下降法计算 Y 和 U 
	U, Y = coordinateDescent(numOfTag, numOfDoc, numOfCode, Y, C, T, theta)

	# 步骤五，计算哈希函数 W
	print 'init identity I for hash function W'
	IM = initI(numOfDimen)
	W = obtainW(numOfDimen, numOfCode, Y, X, IM)

	# # 步骤六，计算中间向量 M
	M = obtainMedianM(numOfDoc, numOfCode, Y)

	# 步骤七，获取哈希编码矩阵 Y
	Y = obtainYViaThresholdMatrixY(numOfDoc, numOfCode, Y, M)

	# 预测过程
	# 输入是一个文档的矩阵 m * n
	q = initMatrixX('./test.data', 7505)
	YQ = np.dot(W, q)
	YQ = obtainYViaThresholdMatrixY(7505, numOfCode, YQ, M)

	print YQ

