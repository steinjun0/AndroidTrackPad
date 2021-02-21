'''
import pywin32_system32

import win32api, win32con
def click(x,y):
    win32api.SetCursorPos((x,y))

for i in range(100):
    click(i*1, i*1)

'''
print(chr(21))