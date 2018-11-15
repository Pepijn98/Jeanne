package info.kurozeropb.sophie.core

class HttpException(httpCode: Int, httpMessage: String) : Exception("HTTP Exception $httpCode $httpMessage")
