package com.shttm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.ujmp.core.matrix.SparseMatrix;


public class ShttmModel {

	/***
	 * 主题个数：9
	 * 文档个数：1791
	 */
	public static final double Epsilon = 1e-6;
	public static final int NumOfDoc_n = 11269;	//文档数1791
	//统计出来的高频词语的个数（这里先糊弄一下）^^^
	public static final int Dimension_m = 61188;
	public static final int NumOfTag_l = 6;	//标签个数9
	public static final int NumOfTop_k = 6;	//lda得到的主题个数9
	//public static final int NumOfHC_len = 8;//8

	
	//[0.5 1 2 4 8 16 32 128]
	//梯度下降法的总迭代次数
	public static final int IterationTimes = 32;
	public static final int ALPHA = 32;
	public static final int GAMMA = 32;
	public static final int LAMTA = 32;
	
	public static final int AValue = 32;
	public static final int BValue = 16;
	
	
	
	//identity矩阵
//	public final double[][] matrix_I = new double[NumOfTop_k][NumOfTop_k];
//	public final double[][] matrix_I_2 = new double[NumOfTag_l][NumOfTag_l];
//	public final double[][] matrix_I_3 = new double[Dimension_m][Dimension_m];
	
	public SparseMatrix matrix_I = SparseMatrix.factory.zeros(NumOfTop_k, NumOfTop_k);
	public SparseMatrix matrix_I_2 = SparseMatrix.factory.zeros(NumOfTag_l, NumOfTag_l);
	public SparseMatrix matrix_I_3 = SparseMatrix.factory.zeros(Dimension_m, Dimension_m);
	
	//输入矩阵X nxm 糊弄^^^
	public SparseMatrix matrix_X = SparseMatrix.factory.zeros(NumOfDoc_n, Dimension_m);
	public SparseMatrix matrix_T = SparseMatrix.factory.zeros(NumOfDoc_n, NumOfTag_l);
	public SparseMatrix matrix_C = SparseMatrix.factory.zeros(NumOfDoc_n, NumOfTag_l);
	public SparseMatrix matrix_C_I = SparseMatrix.factory.zeros(NumOfDoc_n, NumOfDoc_n);
	public SparseMatrix matrix_C_I_2 = SparseMatrix.factory.zeros(NumOfTag_l, NumOfTag_l);
	public SparseMatrix matrix_theta = SparseMatrix.factory.zeros(NumOfDoc_n, NumOfTop_k);
	public SparseMatrix matrix_Y = SparseMatrix.factory.zeros(NumOfDoc_n, NumOfTop_k);
	public SparseMatrix matrix_U = SparseMatrix.factory.zeros(NumOfTag_l, NumOfTop_k);
	public SparseMatrix matrix_W = SparseMatrix.factory.zeros(Dimension_m, NumOfTop_k);
	
////	public double[][] matrix_X = new double[NumOfDoc_n][Dimension_m];
//	//输入-标签矩阵T
//	public double[][] matrix_T = new double[NumOfDoc_n][NumOfTag_l];
//	//置信矩阵C
//	public double[][] matrix_C = new double[NumOfDoc_n][NumOfTag_l];
//	public double[][] matrix_C_I = new double[NumOfDoc_n][NumOfDoc_n];
//	public double[][] matrix_C_I_2 = new double[NumOfTag_l][NumOfTag_l];
//	//主题-文档概率矩阵theta
//	public double[][] matrix_theta = new double[NumOfDoc_n][NumOfTop_k];
//	//哈希矩阵Y
//	public double[][] matrix_Y = new double[NumOfDoc_n][NumOfTop_k];
//	//标签-哈希隐含关联矩阵U
//	public double[][] matrix_U = new double[NumOfTag_l][NumOfTop_k];
//	//哈希函数参数矩阵W
//	public double[][] matrix_W = new double[Dimension_m][NumOfTop_k];
	
	
	
	//中值向量
	public double[] matrix_M = new double[NumOfDoc_n];
	
	
	//初始化X矩阵
	public void setMatrix_X() {
		
		//根据输入获取矩阵
		try{
			
			String pathName1 = "data/x_docid.txt";
			String pathName2 = "data/x_wordid.txt";
			String pathName3 = "data/x_count.txt";
			File x = new File(pathName1);
			File y = new File(pathName2);
			File z = new File(pathName3);
			InputStreamReader reader1 = new InputStreamReader(new FileInputStream(x));
			InputStreamReader reader2 = new InputStreamReader(new FileInputStream(y));
			InputStreamReader reader3 = new InputStreamReader(new FileInputStream(z));
			
			
			BufferedReader br1 = new BufferedReader(reader1);
			BufferedReader br2 = new BufferedReader(reader2);
			BufferedReader br3 = new BufferedReader(reader3);
			String line1, line2, line3;
			
			
			while( (line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null && (line3 = br3.readLine()) != null)
			{
				int i = Integer.parseInt(line1) - 1;
				int j = Integer.parseInt(line2) - 1;
				double value = Double.parseDouble(line3);
//				matrix_X[i][j] = value;
				matrix_X.setAsDouble(value, i, j);
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	//初始化I矩阵
	public void setMatrix_I() {
		for(int i = 0; i < NumOfTop_k; i++)
		{
			for(int j = 0; j < NumOfTop_k; j++)
			{
//				if(i == j)
//					matrix_I[i][j] = 1.0;
//				else
//					matrix_I[i][j] = 0.0;
				if(i == j)
					matrix_I.setAsDouble(1.0, i, j);
			}
		}
	}
	//初始化I矩阵
	public void setMatrix_I_2() {
		for(int i = 0; i < NumOfTag_l; i++)
		{
			for(int j = 0; j < NumOfTag_l; j++)
			{
//				if(i == j)
//					matrix_I_2[i][j] = 1.0;
//				else
//					matrix_I_2[i][j] = 0.0;
				if(i == j)
					matrix_I_2.setAsDouble(1.0, i, j);
			}
		}
	}
	//初始化I矩阵
	public void setMatrix_I_3() {
		for(int i = 0; i < Dimension_m; i++)
		{
			for(int j = 0; j < Dimension_m; j++)
			{
//				if(i == j)
//					matrix_I_3[i][j] = 1.0;
//				else
//					matrix_I_3[i][j] = 0.0;
				if(i == j)
					matrix_I_3.setAsDouble(1.0, i, j);
				
			}
		}
	}
	
	//初始化输入-标签矩阵T
	public void setMatrix_T() {
		
		try{
			String pathName = "data/tag_T.txt";
			File tagFile = new File(pathName);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(tagFile));
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			String[] rec;
			int i = 0;
			while( (line = br.readLine()) != null)
			{
				rec = line.split("\t");
				for(int j = 0; j < 6; j++)
				{
					int a = Integer.parseInt(rec[j]);
					matrix_T.setAsDouble((double)a, i, j);
//					matrix_T[i][j] = (double)a;
				}
				i++;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//初始化置信矩阵C
	public void setMatrix_C() {
		
		for(int i = 0; i < NumOfDoc_n; i++)
		{
			for(int j = 0; j < NumOfTag_l; j++)
			{
				
//				if(Math.abs(matrix_T[i][j] - 1) < Epsilon)
//				matrix_C[i][j] = AValue;
//			else
//				matrix_C[i][j] = BValue;
				if(Math.abs(matrix_T.getAsDouble(i, j) - 1) < Epsilon)
					matrix_C.setAsDouble(AValue, i, j);
				else
					matrix_C.setAsDouble(BValue, i, j);
				

			}
		}
	}
	//初始化C_I矩阵
	public void setMatrix_C_I(int label_row) {
		for(int i = 0; i < NumOfDoc_n; i++)
		{
			for(int j = 0; j < NumOfDoc_n; j++)
			{
//				if(i == j)
//					matrix_C_I[i][j] = matrix_C[i][label_row];
//				else
//					matrix_C_I[i][j] = 0.0;
				
				if(i == j)
					matrix_C_I.setAsDouble(matrix_C.getAsDouble(i, label_row), i, j);

			}
		}
	}
	//初始化C_I矩阵
	public void setMatrix_C_I_2(int doc_col) {
		for(int i = 0; i < NumOfTag_l; i++)
		{
			for(int j = 0; j < NumOfTag_l; j++)
			{
//				if(i == j)
//					matrix_C_I_2[i][j] = matrix_C[doc_col][j];
//				else
//					matrix_C_I_2[i][j] = 0.0;
				if(i == j)
					matrix_C_I_2.setAsDouble(matrix_C.getAsDouble(doc_col,j), i, j);
			}
		}
	}
	
	//初始化主题文档矩阵
	public void setMatrix_theta(double[][] theta) {
		
//		matrix_theta = theta;
		try{
			String pathName1 = "data/theta_doc_id.txt";
			String pathName2 = "data/theta_topic_id.txt";
			String pathName3 = "data/theta_doc_topic_pro.txt";
			File x = new File(pathName1);
			File y = new File(pathName2);
			File z = new File(pathName3);
			InputStreamReader reader1 = new InputStreamReader(new FileInputStream(x));
			InputStreamReader reader2 = new InputStreamReader(new FileInputStream(y));
			InputStreamReader reader3 = new InputStreamReader(new FileInputStream(z));
			
			
			BufferedReader br1 = new BufferedReader(reader1);
			BufferedReader br2 = new BufferedReader(reader2);
			BufferedReader br3 = new BufferedReader(reader3);
			String line1, line2, line3;
			
			
			while( (line1 = br1.readLine()) != null && (line2 = br2.readLine()) != null && (line3 = br3.readLine()) != null)
			{
				int i = Integer.parseInt(line1) - 1;
				int j = Integer.parseInt(line2) - 1;
				double value = Double.parseDouble(line3);
				matrix_theta.setAsDouble(value, i, j);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	//初始化哈希矩阵Y
	public void setMatrix_Y(double[][] theta){
		
//		matrix_Y = theta;
		matrix_Y = matrix_theta;
		
	}
	
}
