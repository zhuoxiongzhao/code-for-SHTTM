package com.shttm.main;

import java.io.IOException;
import java.util.Arrays;

import org.ujmp.core.matrix.SparseMatrix;
import org.ujmp.core.util.MathUtil;

import Jama.Matrix;

import com.shttm.util.ShttmModel;

import main.java.com.hankcs.lda.Corpus;
import main.java.com.hankcs.lda.LdaGibbsSampler;
import main.java.com.hankcs.lda.LdaUtil;


/***
 * 这里作为第一个run
 * 申明：
 * 由于文档分析困难，这里仅提供模拟运行。
 * 模拟运行的合理假设有：
 * (1)输入矩阵X
 * (2)T矩阵是标签矩阵
 * @author HP
 *
 */
public class FirstRun {

	public static void main(String[] args) throws IOException
	{
		
		//Train训练模型
		//初始化模型
		ShttmModel sm = new ShttmModel();
		initialization(sm);
		coordinate_descent(sm);
		optimation_W(sm);
		//对Y计算得到中值向量m nx1
		getMedian_M(sm);
		//阈值化处理Y
		threshold_Y(sm);
		//输出最终结果Y
		print(sm);
		
		//Test测试模型-投票决策
		
		
	}
	
	public static void initialization(ShttmModel sm) throws IOException {
		
		//获取文档-主题概率数据theta
		//获得wordlist，得到文档的词语的id列表
	    Corpus corpus = Corpus.load("data/mini");
	    LdaGibbsSampler ldaGibbsSampler = new LdaGibbsSampler(corpus.getDocument(), corpus.getVocabularySize());
	    ldaGibbsSampler.gibbs(sm.NumOfTop_k);
	    double[][] phi = ldaGibbsSampler.getPhi();
	    double[][] theta = ldaGibbsSampler.getTheta();
	    
		//初始化模型
	    //先搞到X T M
	    sm.setMatrix_X();
	    sm.setMatrix_I();
	    sm.setMatrix_I_2();
		sm.setMatrix_theta(theta);
		sm.setMatrix_T();
		sm.setMatrix_C();
		sm.setMatrix_Y(theta);
		
	}
	
	public static void coordinate_descent(ShttmModel sm){
	
		//梯度下降法训练得到Y和U
		int k = 0;
		while(++k < sm.IterationTimes) {
			computeU(sm);
			computeY(sm);
		}
	}
	private static void computeU(ShttmModel sm) {
		//引入矩阵包进行矩阵
		
		
		
//		Matrix mat_Y = new Matrix(sm.matrix_Y).transpose();
//		Matrix mat_I = new Matrix(sm.matrix_I);
//		Matrix mat_T = new Matrix(sm.matrix_T);
		
		for(int i = 0; i < sm.NumOfTag_l; i++)
		{
			sm.setMatrix_C_I(i);
//			Matrix mat_C_I = new Matrix(sm.matrix_C_I);
//			Matrix mat_U = (mat_Y.times(mat_C_I).times(mat_Y.transpose()).plus(mat_I.times(sm.ALPHA))).inverse();
//			mat_U = mat_U.times(mat_Y).times(mat_C_I).times(mat_T);
//			//更新sm的矩阵U
//			for(int j = 0; j < sm.NumOfTop_k; j++)
//				sm.matrix_U[i][j] = mat_U.get(j, i);
			SparseMatrix mat_U = SparseMatrix.factory.zeros(sm.NumOfTop_k, sm.NumOfTop_k);
			SparseMatrix mat_U2 = SparseMatrix.factory.zeros(sm.NumOfTop_k, sm.NumOfTag_l);
			mat_U = (SparseMatrix) ((sm.matrix_Y).transpose().times(sm.matrix_C_I).times(sm.matrix_Y)).plus(sm.matrix_I.times(sm.ALPHA)).inv(); 
			mat_U2 = (SparseMatrix) mat_U.times(sm.matrix_Y.transpose()).times(sm.matrix_C_I).times(sm.matrix_T);
			for(int j = 0; j < sm.NumOfTop_k; j++)
				sm.matrix_U.setAsDouble(mat_U2.getAsDouble(j, i), i, j);
			
		}
		
	}

	private static void computeY(ShttmModel sm) {
		
//		Matrix mat_I = new Matrix(sm.matrix_I);
//		Matrix mat_T = new Matrix(sm.matrix_T).transpose();
//		Matrix mat_U = new Matrix(sm.matrix_U).transpose();
//		Matrix mat_theta = new Matrix(sm.matrix_theta).transpose();
		
		for(int i = 0; i < sm.NumOfDoc_n; i++)
		{
			sm.setMatrix_C_I_2(i);
//			Matrix mat_C_I_2 = new Matrix(sm.matrix_C_I_2);
			
//			Matrix mat_Y_pre = (mat_U.times(mat_C_I_2).times(mat_U).plus(mat_I.times(sm.GAMMA))).inverse();
//			Matrix mat_Y_ahe = (mat_U.times(mat_C_I_2).times(mat_T).plus(mat_theta.times(sm.GAMMA)));
//			Matrix mat_Y = mat_Y_pre.times(mat_Y_ahe);
//			
//			//更新sm的矩阵Y
//			for(int j = 0; j < sm.NumOfTop_k; j++)
//				sm.matrix_Y[i][j] = mat_Y.get(j, i);
			org.ujmp.core.Matrix mat_Y_pre = (sm.matrix_U.times(sm.matrix_C_I_2).times(sm.matrix_U.transpose()).plus(sm.matrix_I.times(sm.GAMMA))).inv();
			org.ujmp.core.Matrix mat_Y_ahe = (sm.matrix_U.times(sm.matrix_C_I_2).times(sm.matrix_T.transpose()).plus(sm.matrix_theta.transpose().times(sm.GAMMA)));
			sm.matrix_Y = (SparseMatrix) mat_Y_pre.times(mat_Y_ahe);
		}

	}
	public static void optimation_W(ShttmModel sm) {
		
		//获取优化后的W
//		Matrix mat_Y = new Matrix(sm.matrix_Y).transpose();
//		Matrix mat_X = new Matrix(sm.matrix_X);
//		Matrix mat_I_3 = new Matrix(sm.matrix_I_3);
//		Matrix mat_W = mat_Y.times(mat_X).times((mat_X.transpose().times(mat_X).plus(mat_I_3.times(sm.LAMTA))));
//		for(int i = 0; i < sm.Dimension_m; i++)
//		{
//			for(int j = 0; j < sm.NumOfTop_k; j++)
//				sm.matrix_W[i][j] = mat_W.get(j, i);
//		}
		org.ujmp.core.Matrix mat_W = sm.matrix_Y.transpose().times(sm.matrix_X).times((sm.matrix_X.transpose().times(sm.matrix_X).plus(sm.matrix_I_3.times(sm.LAMTA))));
		for(int i = 0; i < sm.Dimension_m; i++)
		{
			for(int j = 0; j < sm.NumOfTop_k; j++)
				sm.matrix_W.setAsDouble(mat_W.getAsDouble(j, i), i, j);
		}
		
	}
	public static void getMedian_M(ShttmModel sm) {
		//处理得到Y每一行的树的中值
		//实际就是排个序，然后去中间那个数的大小
		double[][] arr1 = new double[sm.NumOfTop_k][];
		for(int i = 0; i < sm.NumOfDoc_n; i++)
			for(int j = 0; j < sm.NumOfTop_k; j++)
				arr1[i][j] = sm.matrix_Y.getAsDouble(i, j);
		for(int i = 0; i < sm.NumOfDoc_n; i++)
		{

			Arrays.sort(arr1[i]);
//			if(sm.NumOfTop_k % 2 == 0)
//			{
//				//排序后中间两个数的平均
//				sm.matrix_M[i] = (arr1[(sm.NumOfTop_k / 2 - 1)] + arr1[(sm.NumOfTop_k / 2) ]) / 2.0;
//			}
//			else
//			{
//				//排序后第sm.NumOfTop_k / 2个
//				sm.matrix_M[i] = arr1[sm.NumOfTop_k / 2];
//			}
			if(sm.NumOfTop_k % 2 == 0)
			{
				//排序后中间两个数的平均
				sm.matrix_M[i] = (arr1[i][(sm.NumOfTop_k / 2 - 1)] + arr1[i][(sm.NumOfTop_k / 2) ]) / 2.0;
			}
			else
			{
				//排序后第sm.NumOfTop_k / 2个
				sm.matrix_M[i] = arr1[i][sm.NumOfTop_k / 2];
			}

		}
		
	}
	public static void threshold_Y(ShttmModel sm) {
//		//Y nxk
//		for(int i = 0; i < sm.NumOfDoc_n; i++ )
//		{
//			for(int j = 0; j < sm.NumOfTop_k; j++)
//			{
//				if(sm.matrix_Y[i][j] > sm.matrix_M[i])
//					sm.matrix_Y[i][j] = 1.0;
//				else
//					sm.matrix_Y[i][j] = -1.0;
//			}
//		}
		for(int i = 0; i < sm.NumOfDoc_n; i++ )
		{
			for(int j = 0; j < sm.NumOfTop_k; j++)
			{
				if(sm.matrix_Y.getAsDouble(i, j) > sm.matrix_M[i])
					sm.matrix_Y.setAsDouble(1.0, i, j);
			}
		}
	}
	
	public static void print(ShttmModel sm) {
		//print Y
		for(int i = 0; i < sm.NumOfDoc_n; i++ )
		{
			for(int j = 0; j < sm.NumOfTop_k; j++)
			{
//				System.out.print(sm.matrix_Y[i][j]+"\t");
				System.out.print(sm.matrix_Y.getAsDouble(i, j) + "\t");
			}
			System.out.print("\n");
		}
	}
	
}
