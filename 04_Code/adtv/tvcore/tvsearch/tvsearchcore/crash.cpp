#include <stdio.h>

int test_crash2(int *p)
{
	int i;
	i=*p;
	return i;
}

int test_crash(int *p)
{
	int j;
	j=test_crash2(p);
	return j;
}

int main(int argc, char *argv[])
{
	printf("hello 中文\n");
//	int k = test_crash(0);	// 传入一个非法地址
//	printf("k is %d\n",k);
	return 0;
}
