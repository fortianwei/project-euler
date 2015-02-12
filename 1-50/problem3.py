# coding=utf-8
'''
Largest prime factor
Problem 3

The prime factors of 13195 are 5, 7, 13 and 29.

What is the largest prime factor of the number 600851475143 ?
'''
import itertools
def f(n):
    for i in itertools.count(int(n**0.5), -1):
        if n % i == 0 and isPrime(i):
            return i

#质数就是算(2~sqrt(n))之间有没有能被除尽的
def isPrime(n):
    for i in xrange(2, int(n**0.5)+1):
        if n % i == 0:
            return False
    else:
        return True

print f(39)#有问题！

def nwd(n):
    if n == 1:
        lpf = 1
    else:
        lpf = 2
        while n != lpf:
            if n % lpf == 0:
                n /= lpf
            else:
                lpf += 1
    return lpf
print nwd(39)

