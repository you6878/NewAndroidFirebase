package com.howlab.newfirebase

data class UserModel(var age : String? = null,
                     var name : String? = null,
                     var likeCount : Int? = 0,
                     var likes : MutableMap<String,Any> = mutableMapOf())