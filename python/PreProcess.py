#!/usr/bin/env python
# -*- coding: utf-8 -*-

__author__ = 'xiexiaochao'

import numpy as np
import time

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


def initX(filePath) :
	# 查找文档最大维度
	X = np.zeros((findDemin(filePath), 11269))
	fp = open(filePath, "r")
	for eachline in fp:
		subs = eachline.split(' ')
		X[int(subs[1]) - 1][int(subs[0]) - 1] = float(subs[2])
	return X

def initT(filePath) :
	index = 0
	docId = 0
	T = np.zeros((6, 11269))
	fp = open(filePath, "r")
	for eachline in fp:
		labelId = int(eachline)
		if labelId > 1 and labelId < 7:
			index = 0
		elif labelId > 7 and labelId < 12:
			index = 1
		elif labelId > 11 and labelId < 16:
			index = 2
		elif labelId == 7:
			index = 3
		elif labelId > 16 and labelId < 20:
			index = 4
		else :
			index = 5
		T[index][docId] = 1.0
		docId += 1
	return T

T = initT('./train.label')
print T.shape
print T.transpose().shape








