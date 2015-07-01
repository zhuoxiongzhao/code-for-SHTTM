#CPP version for SHTTM

---

Database -- http://qwone.com/~jason/20Newsgroups/

Eigen -- http://eigen.tuxfamily.org/index.php?title=Main_Page  矩阵运算库

编译代码 -- 分两步

1. g++ -Wall -I ./eigen -std=c++14 -o main.cpp main
2. g++ -Wall -I ./eigen -std=c++14 -o test.cpp test

说明:

test.label, test.data, train.label, train.data来自数据库。

main.cpp 是使用train的SHTTM算法。运行后将生成W.data, U.data, M.data, YH.data

test.cpp 将利用main.cpp生成的模型对test数据集进行测试并计算精度。

Power by Jun Hu<br/>
Please contact e-mail: kuyasinaki@gmail.com

