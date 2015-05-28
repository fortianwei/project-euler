#coding=utf-8
'''
Even Fibonacci numbers
Problem 2

Each new term in the Fibonacci sequence is generated by adding the previous two terms. By starting with 1 and 2, the first 10 terms will be:

1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...

By considering the terms in the Fibonacci sequence whose values do not exceed four million, find the sum of the even-valued terms.

'''

def fn1(x):
    if x == 1:
        return 1
    elif x == 2:
        return 2
    else:
        return fn1(x-2) + fn1(x-1)

def fn2(n):
    return int(1/5**0.5*(((1+5**0.5)/2)**(n+2) - ((1-5**0.5)/2)**(n+2)))


from itertools import count


def testWithRecursive():
    total = 0
    for i in count(1):
        temp = fn1(i)
        if temp > 4000000:
            break
        if temp % 2 ==0:
            total += temp
    print total

def testWithMathmatics():
    total = 0
    for i in count(1):
        temp = fn2(i)
        if temp > 4000000:
            break
        if temp % 2 == 0:
            total += temp
    print total

#使用生成器的fibonacci
def fibonacci():
    m, n = 1, 2
    while True:
        yield n
        m, n = n, m + n

def testWithGenerator():
    total = 0
    fib = fibonacci()
    for i in count(1):
        temp = fib.next()
        if temp > 4000000:
            break
        if temp % 2 ==0:
            total += temp
    print total

import timeit

t1 = timeit.Timer('testWithRecursive()', 'from __main__ import testWithRecursive')
t2 = timeit.Timer('testWithGenerator()', 'from __main__ import testWithGenerator')
t3 = timeit.Timer('testWithMathmatics()', 'from __main__ import testWithMathmatics')

print 'testWithRecursive', t1.timeit(1)
print 'testWithGenerator', t2.timeit(1)
print 'testWithMathmatics', t3.timeit(1)


'''
斐波那契数列：0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, ...

F(n)=(1/√5)*{[(1+√5)/2]^n - [(1-√5)/2]^n}【√5表示根号5】

采用公式比直接用计算机递归要速度快得多，所以说：数学很重要！

但是，使用生成器，直接每次利用上次的计算结果，能更加快速的计算下一个数值，比纯数学的方式要快上一倍！
'''