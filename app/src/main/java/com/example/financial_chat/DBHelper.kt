package com.example.financial_chat

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class DBHelper {
//    SQLiteOpenHelper(context, "Login.db", null, 1) {
//        // users 테이블 생성
//        override fun onCreate(MyDB: SQLiteDatabase?) {
//            MyDB!!.execSQL("create Table users(id TEXT primary key, password TEXT, nick TEXT, phone TEXT)")
//        }
//
//        // 정보 갱신
//        override fun onUpgrade(MyDB: SQLiteDatabase?, i: Int, i1: Int) {
//            MyDB!!.execSQL("drop Table if exists users")
//        }
//
//        // id, password, nick, phone 삽입 (성공시 true, 실패시 false)
//        fun insertData (id: String?, password: String?, nick: String?, phone: String?): Boolean {
//            val MyDB = this.writableDatabase
//            val contentValues = ContentValues()
//            contentValues.put("id", id)
//            contentValues.put("password", password)
//            contentValues.put("nick", nick)
//            contentValues.put("phone", phone)
//            val result = MyDB.insert("users", null, contentValues)
//            MyDB.close()
//            return if (result == -1L) false else true
//        }
//
//        // 사용자 아이디가 없으면 false, 이미 존재하면 true
//        fun checkUser(id: String?): Boolean {
//            val MyDB = this.readableDatabase
//            var res = true
//            val cursor = MyDB.rawQuery("Select * from users where id =?", arrayOf(id))
//            if (cursor.count <= 0) res = false
//            return res
//        }
//
//        // 사용자 닉네임이 없으면 false, 이미 존재하면 true
//        fun checkNick(nick: String?): Boolean {
//            val MyDB = this.readableDatabase
//            var res = true
//            val cursor = MyDB.rawQuery("Select * from users where nick =?", arrayOf(nick))
//            if (cursor.count <= 0) res = false
//            return res
//        }
//
//        // 해당 id, password가 있는지 확인 (없다면 false)
//        fun checkUserpass(id: String, password: String) : Boolean {
//            val MyDB = this.writableDatabase
//            var res = true
//            val cursor = MyDB.rawQuery(
//                "Select * from users where id = ? and password = ?",
//                arrayOf(id, password)
//            )
//            if (cursor.count <= 0) res = false
//            return res
//        }
//
//        // DB name을 Login.db로 설정
//        companion object {
//            const val DBNAME = "Login.db"
//        }
}

// 참고: https://velog.io/@hyhy0623/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%8A%A4%ED%8A%9C%EB%94%94%EC%98%A4-%EC%BD%94%ED%8B%80%EB%A6%B0-%EA%B8%B0%EB%B3%B8%EC%A0%81%EC%9D%B8-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0