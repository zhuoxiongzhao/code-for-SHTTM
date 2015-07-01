SHTTM - JAVA
========

A Java implemention of SHTTM
How To Use
--

 - code
 
```java
public static void main(String[] args)
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
}
```
 - output
```
    Hashing code for each Document
```
 - LDA
```
    LDA Gibbs Sampling
```
