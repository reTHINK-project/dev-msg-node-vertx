export default class VertxProtoStub {
  /* private
    _runtimeProtoStubURL: string
    _msgNodeURL: string
    _msgCallback: (Message) => void
    _sock: (WebSocket | SockJS)
  */

  /* note
  1. a message is needed for runtime registration / protostub registration?
  2. is the runtime registration managed by the ProtoStub? This is probably a need, because of connection recovery! So, it must save info about this registration message.
  3. there is no msgNodeURL on the ProtoStub interface definition!
  4. there are specific messages sent to the MessageBus that returns the ProtoStub status.
   */

  constructor(runtimeProtoStubURL, busPostMessage, configuration) {
    this._runtimeProtoStubURL = runtimeProtoStubURL;
    this._msgCallback = busPostMessage;

    this._msgNodeURL = configuration.url;
  }

  get url() { return this._msgNodeURL; }

  postMessage(msg) {
    let _this = this;

    _this._open(() => {
      _this._sock.send(JSON.stringify(msg));
    });
  }

  disconnect() {
    if (this._sock) {
      this._sock.close();
    }
  }

  _waitReady(callback) {
    let _this = this;

    if (_this._sock.readyState === 1) {
      callback();
    } else {
      setTimeout(() => {
        _this._waitReady(callback);
      });
    }
  }

  _open(callback) {
    let _this = this;

    if (!_this._sock) {
      if (_this._msgNodeURL.substring(0, 2) === 'ws') {
        _this._sock = new WebSocket(_this._msgNodeURL);
      } else {
        _this._sock = new SockJS(_this._msgNodeURL);
      }

      _this._sock.onopen = function() {
        _this._msgCallback({
          header: {
            type: 'update',
            from: _this._runtimeProtoStubURL,
            to: _this._runtimeProtoStubURL + '/status'
          },
          body: {
            value: 'connected'
          }
        });

        callback();
      };

      _this._sock.onmessage = function(e) {
        var msg = JSON.parse(e.data);
        _this._msgCallback(msg);
      };

      _this._sock.onclose = function(e) {
        let reason;

        //See https://tools.ietf.org/html/rfc6455#section-7.4
        if (event.code == 1000) {
          reason = 'Normal closure, meaning that the purpose for which the connection was established has been fulfilled.';
        } else if (event.code == 1001) {
          reason = 'An endpoint is \'going away\', such as a server going down or a browser having navigated away from a page.';
        } else if (event.code == 1002) {
          reason = 'An endpoint is terminating the connection due to a protocol error';
        } else if (event.code == 1003) {
          reason = 'An endpoint is terminating the connection because it has received a type of data it cannot accept (e.g., an endpoint that understands only text data MAY send this if it receives a binary message).';
        } else if (event.code == 1004) {
          reason = 'Reserved. The specific meaning might be defined in the future.';
        } else if (event.code == 1005) {
          reason = 'No status code was actually present.';
        } else if (event.code == 1006) {
          reason = 'The connection was closed abnormally, e.g., without sending or receiving a Close control frame';
        } else if (event.code == 1007) {
          reason = 'An endpoint is terminating the connection because it has received data within a message that was not consistent with the type of the message (e.g., non-UTF-8 [http://tools.ietf.org/html/rfc3629] data within a text message).';
        } else if (event.code == 1008) {
          reason = 'An endpoint is terminating the connection because it has received a message that "violates its policy". This reason is given either if there is no other sutible reason, or if there is a need to hide specific details about the policy.';
        } else if (event.code == 1009) {
          reason = 'An endpoint is terminating the connection because it has received a message that is too big for it to process.';
        } else if (event.code == 1010) {
          reason = 'An endpoint (client) is terminating the connection because it has expected the server to negotiate one or more extension, but the server didn\'t return them in the response message of the WebSocket handshake. <br /> Specifically, the extensions that are needed are: ' + event.reason;
        } else if (event.code == 1011) {
          reason = 'A server is terminating the connection because it encountered an unexpected condition that prevented it from fulfilling the request.';
        } else if (event.code == 1015) {
          reason = 'The connection was closed due to a failure to perform a TLS handshake (e.g., the server certificate can\'t be verified).';
        } else {
          reason = 'Unknown reason';
        }

        _this._msgCallback({
          header: {
            type: 'update',
            from: _this._runtimeProtoStubURL,
            to: _this._runtimeProtoStubURL + '/status'
          },
          body: {
            value: 'disconnected',
            desc: reason
          }
        });

        delete _this._sock;
      };
    } else {
      _this._waitReady(callback);
    }
  }
}
