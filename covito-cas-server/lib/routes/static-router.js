"use strict";

var main=require("../../controllers/main");

var setRequestUrl=function(app){
    app.get('/', main.index);
}

exports.setRequestUrl=setRequestUrl;