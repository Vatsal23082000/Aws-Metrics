# importing the required module
from turtle import color
import matplotlib.pyplot as plt
import matplotlib as mpl
import numpy as np

def hello():
    
    plt.style.use(['dark_background'])
    font = {'size': 8}
    mpl.rcParams['lines.linewidth'] = 2
    # using rc function
    plt.rc('font', **font)

    with open('x.txt', 'r') as file:
        lines = file.readlines()
    
    with open('y.txt', 'r') as file1:
        lines1 = file1.readlines()
    # x axis values
    x = lines
    # corresponding y axis values
    y1 = lines1



    
    y=[np.double(j) for j in y1]
    plt.xticks(np.arange(0, len(x) + 1, 10))
    plt.xlabel("Date and Time",fontsize=15)
    plt.ylabel("Value",fontsize=15)
    plt.title("Metrics",fontsize=20)



    with plt.style.context('dark_background'):
        plt.plot(x,y,'-yo')

    plt.show()
hello()