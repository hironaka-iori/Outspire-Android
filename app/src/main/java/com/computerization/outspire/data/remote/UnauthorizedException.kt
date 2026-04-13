package com.computerization.outspire.data.remote

class UnauthorizedException(val statusCode: Int) : RuntimeException("TSIMS unauthorized: $statusCode")
