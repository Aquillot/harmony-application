package fr.harmony

class User {
    var username: String = ""
    var email: String = ""
    var id: Int = -1

    constructor(username: String, email: String, userId: Int) {
        this.username = username
        this.email = email
        this.id = userId
    }

    constructor() {}
}