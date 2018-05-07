package com.example.mayank.nfcapp.network

/**
 * Created by Mayank on 17/03/2018.
 */
open class CommonResult {

    var isSuccess: Boolean? = null
    lateinit var message: String

    constructor() {

    }

    constructor(isSuccess: Boolean?, message: String) {
        this.isSuccess = isSuccess
        this.message = message
    }
}