package com.example.mayank.nfcapp

/**
 * Created by Mayank on 26/03/2018.
 */
class Error {

    lateinit var message: String
    lateinit var error_description: String
    var errorCode = 0

    val messageWithCode: String
        get() = errorCode.toString() + " - " + message

    val messageWithDesc: String
        get() = errorCode.toString() + " - " + message + "\nDescription : " + error_description

    constructor() {}

    constructor(message: String) {
        this.message = message
    }

    constructor(errorCode: Int, message: String) {
        this.errorCode = errorCode
        this.message = message
    }

    constructor(errorCode: Int, message: String, errorDescription: String) {
        this.errorCode = errorCode
        this.message = message
        this.error_description = errorDescription
    }
}