// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
//
// The Promise code are modified from https://gist.github.com/unscriptable/814052
// with original copyright and license as below.
//
// (c) copyright unscriptable.com / John Hann
// License MIT

var _promises = {};
var _next_promise_id = 0;
var _listeners = {};
var _next_listener_id = 0;

function Promise() {
  this._dones = [];
}

Promise.prototype = {
  done: function(onFulfilled, onRejected) {
    this._dones.push({fulfill: onFulfilled, reject: onRejected});
    return this;
  },
  fulfill: function(value) {
    this._finish('fulfill', value);
  },
  reject: function(error) {
    this._finish('reject', error);
  },
  _finish: function(which, arg) {
    // Cover and sync func `done()`.
    this.done = which === 'fulfill' ?
      function(fulfill, reject) {fulfill && fulfill(arg); return this;} :
      function(fulfill, reject) {reject && reject(arg); return this;};
    // Disallow multiple calls.
    this.fulfill = this.reject =
      function() {throw new Error('Promise already completed.');}
    // Complete all async `done()`s.
    var done, i = 0;
    while (done = this._dones[i++]) {
      done[which] && done[which](arg);
    }
    delete this._dones;
  }
};

var postMessage = function(msg) {
  var p = new Promise();

  _promises[_next_promise_id] = p;
  msg._promise_id = _next_promise_id.toString();
  _next_promise_id += 1;

  extension.postMessage(JSON.stringify(msg));
  return p;
};

function _addConstProperty(obj, propertyKey, propertyValue) {
  Object.defineProperty(obj, propertyKey, {
    configurable: false,
    writable: false,
    value: propertyValue
  });
}

function _createConstClone(obj) {
  var const_obj = {};
  for (var key in obj) {
    if (Array.isArray(obj[key])) {
      var obj_array = obj[key];
      var const_obj_array = [];
      for (var i = 0; i < obj_array.length; ++i) {
        var const_sub_obj = {};
        for (var sub_key in obj_array[i]) {
          _addConstProperty(const_sub_obj, sub_key, obj_array[i][sub_key]);
        }
        const_obj_array.push(const_sub_obj);
      }
      _addConstProperty(const_obj, key, const_obj_array);
    } else {
      _addConstProperty(const_obj, key, obj[key]);
    }
  }
  return const_obj;
}

extension.setMessageListener(function(json) {
  var msg = JSON.parse(json);

  if (msg.data.error) {
    _promises[msg._promise_id].reject(msg.data.error);
  } else {
    _promises[msg._promise_id].fulfill(_createConstClone(msg.data));
  }

  delete _promises[msg._promise_id];
});

exports.save = function(contact) {
  var msg = {};
  msg['cmd'] = 'save';
  msg['contact'] = contact;
  return postMessage(msg);
};
