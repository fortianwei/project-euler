#encoding=utf-8
'''
Multiples of 3 and 5
Problem 1

If we list all the natural numbers below 10 that are multiples of 3 or 5, we get 3, 5, 6 and 9. The sum of these multiples is 23.

Find the sum of all the multiples of 3 or 5 below 1000.

'''

print sum(filter(lambda x: x % 3 == 0 or x % 5 == 0, xrange(1, 1000)))

'''
上面是编程的思考方法,但是呢,如果用数学的思考方法呢?
如果不是1000,而是N
那么在N里面
3的倍数的数总共有(int)(N/3)个,设为A
5的倍数的数总共有(int)(N/5)个,设为B
15的倍数的数总共有(int)(N/15)个,设为C

sum = 3的倍数的数和+5的倍数的数和-15的倍数的数和

3的倍数的总和 = 3+6+9+...+A*3 = 3*(1+2+3+...+A) = 3*(A*(A+1)/2)
5的和15的类似
所以最终是 3*(A*(A+1)/2) + 5*(B*(B+1))/2 - 15*((C*(C+1)/2)
'''
N = 1000
print 3*(int(N/3)*((int(N/3))+1))/2 + 5*(int(N/5)*((int(N/5))+1))/2 - 15*(int(N/15)*((int(N/15))+1))/2