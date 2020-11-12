package dev.vdbroek.jeanne.core

class HttpException(httpCode: Int, httpMessage: String) : Exception("HTTP Exception $httpCode $httpMessage")
