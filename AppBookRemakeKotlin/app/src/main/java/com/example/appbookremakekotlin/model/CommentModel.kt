package com.example.appbookremakekotlin.model

class CommentModel {

    var id = ""
    var bookId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    constructor()

    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }


}