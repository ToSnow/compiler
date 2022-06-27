# 项目目标

------

## 声明

本项目仅供参考，未经过完整的测试，可能仍有部分遗留问题，不对程序的正确性做任何保证。
本项目可能与部分学校的课程设计有所重合。开源不是鼓励鼓励抄袭，任何形式的Ctrl-C/V行为请自行承担责任。

------

## 词法分析

#### **程序输入：**

1. 一组 **3º型文法**（正规文法）的产生式
2. 含一组需要识别的**字符串**（程序代码）

#### **程序输出：**

token（令牌）表，包含该token所在的行列、token类型及具体内容。该表由 6 种类型 token 组成：**关键词**，**标识符**，**常量**，**限定符**，**界符**和**运算符**

#### 过程简述：

1. 根据用户输入的正规文法，生成 NFA。
2. 根据生成的NFA，进行确定化，获得DFA。
3. 根据 DFA 转换图，从头到尾从左至右识别用户输入的源代码，生成 token 列表（三元组：所在行号，类别，token 内容）。

#### 额外要求：

- 可以准确识别科学计数法形式的常量（如 0.314E+1）及复数常量（如 10+12i）。
- 能检查整数常量的合法性，标识符的合法性（首字符不能为数字等）。
- 尽量符合真 实常用高级语言要求的规则。

------

## 语法分析

#### 程序输入：

1. 2º型文法（上下文无关文法）的产生式集 合
2. 词法分析程序输出的（生成的）token 令牌表

#### 程序输出：

TRUE 或 FALSE（源代码字符串符合此 2º型文法，或者源代码字符串不符合此 2º型文法）。

如存在错误，则标示出错行号，并给出大致的出错原因。

#### 过程简述：

根据用户输入的 2º型文法，生成 Action 及 Goto 表，设计合适的数据结构，判断 token 序列（用户输入的源程序转换）。

