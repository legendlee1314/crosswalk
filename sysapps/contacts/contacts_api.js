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

extension.setMessageListener(function(json) {
  var msg = JSON.parse(json);
  _promises[msg._promise_id].fulfill(msg.data);
  delete _promises[msg._promise_id];
});

exports.save = function(contact) {
  var msg = {};
  msg['cmd'] = 'save';
  msg['contact'] = contact;
  return postMessage(msg);
};

exports.find = function(options) {
  var msg = {};
  msg['cmd'] = 'find';
  msg['options'] = options;
  return postMessage(msg);
};

exports.remove = function(contactId) {
  var msg = {};
  msg['cmd'] = 'remove';
  msg['contactId'] = contactId;
  return postMessage(msg);
};
