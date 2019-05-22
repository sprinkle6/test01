package com.qint.pt1.domain

//Action是用户执行的一个操作，该操作会改变用户（当前及其他）或周围环境（如当前聊天室）的状态。
//动作结果带来的状态变更如果需要知会其他用户，使用Message。
//TODO: Action是否可以作为一个UseCase的子类。
//TODO：Action是否要分为
interface Action