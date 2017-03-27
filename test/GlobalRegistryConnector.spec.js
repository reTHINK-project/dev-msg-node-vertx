import expect from 'expect.js';
import activate from '../src/js/client/VertxProtoStub';

describe('GlobalRegistryConnector', function() {
	let protoURL = 'hyperty-runtime://ua.pt/123/graph-connector';

	let config = {
		url: 'wss://msg-node.ua.pt:9090/ws',
		runtimeURL: 'runtime:/alice1'
	};

	it('update record', function(done) {

		this.timeout(20000);

		let send;

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 64) {
					expect(msg).to.eql({
						id: 64, type: 'response', from: 'global://registry/', to: protoURL,
						body: { via: protoURL, message: 'request was performed successfully', responseCode: 200, errorCode: 0}
					});

					done();
				}
			},

			addListener: (url, callback) => {
				console.log('addListener: ', url);
				send = callback;
			}
		};

		let proto = activate(protoURL, bus, config).activate;

		send({
			id: 64, type: 'CREATE', from: "hyperty-runtime://ua.pt/123/graph-connector", to: 'global://registry/',
			body: { guid: 'puZE5qCSGqcjg5mViBM3CdQHKIcHpRoyF3OGLTaYzGs', jwt: 'eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pY0hWYVJUVnhRMU5IY1dOcVp6VnRWbWxDVFRORFpGRklTMGxqU0hCU2IzbEdNMDlIVEZSaFdYcEhjeUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZUTFVoek5ubG5RVEZpWm05c1kwMVBSR2RRT1ZkR1dqSlVTVFp3Tm1VNUxVMUxabEJuV1d0Mk1FVjNhSEF5T1ROVmRGOUdlbTVUWlV0dlRqQTRNemd3YkRCU1NGbDRabTlxTmxacVREaE9XRGxOVGtaa1p5MHRMUzB0UlU1RUlGQlZRa3hKUXlCTFJWa3RMUzB0TFNJc0lteGhjM1JWY0dSaGRHVWlPaUl5TURFMUxUQTVMVEkwVkRBNE9qSTBPakkzS3pBd09qQXdJaXdpWVdOMGFYWmxJam94TENKMWMyVnlTVVJ6SWpwYkluSmxWRWhKVGtzNkx5OXpaV0poYzNScFlXNHVaMjlsYm1SdlpYSXVibVYwTHlJc0luSmxWRWhKVGtzNkx5OW1ZV05sWW05dmF5NWpiMjB2Wm14MVptWjVNVEl6SWwwc0luSmxkbTlyWldRaU9qQXNJblJwYldWdmRYUWlPaUl5TURJMkxUQTVMVEkwVkRBNE9qSTBPakkzS3pBd09qQXdJaXdpYzJGc2RDSTZJbE53U0hWWWQwVkhkM0pPWTBWalJtOU9VemhMZGpjNVVIbEhSbXg0YVRGMkluMCJ9.MEYCIQCxuGtdM8HqcM0G-PxT7mGKUM6-cMaCiLF4PtT2aGQXRwIhAM6z5t5f1Mpgjw74mXL-NJQ8NcaX_SYTVyGmR8XIVCcF' }
		});
	});

	it('get user', function(done) {

		this.timeout(20000);

		let send;

		let bus = {
			postMessage: (msg) => {
				console.log('postMessage: ', JSON.stringify(msg));

				if (msg.id === 71) {
					expect(msg).to.eql({
						id: 71, type: 'response', from: 'global://registry/', to: protoURL,
						body: { via: protoURL, message: 'request was performed successfully', responseCode: 200, errorCode: 0,
						data: 'eyJhbGciOiJFUzI1NiJ9.eyJkYXRhIjoiZXlKbmRXbGtJam9pY0hWYVJUVnhRMU5IY1dOcVp6VnRWbWxDVFRORFpGRklTMGxqU0hCU2IzbEdNMDlIVEZSaFdYcEhjeUlzSW5CMVlteHBZMHRsZVNJNklpMHRMUzB0UWtWSFNVNGdVRlZDVEVsRElFdEZXUzB0TFMwdFRVWlpkMFZCV1VoTGIxcEplbW93UTBGUldVWkxORVZGUVVGdlJGRm5RVVZUTFVoek5ubG5RVEZpWm05c1kwMVBSR2RRT1ZkR1dqSlVTVFp3Tm1VNUxVMUxabEJuV1d0Mk1FVjNhSEF5T1ROVmRGOUdlbTVUWlV0dlRqQTRNemd3YkRCU1NGbDRabTlxTmxacVREaE9XRGxOVGtaa1p5MHRMUzB0UlU1RUlGQlZRa3hKUXlCTFJWa3RMUzB0TFNJc0lteGhjM1JWY0dSaGRHVWlPaUl5TURFMUxUQTVMVEkwVkRBNE9qSTBPakkzS3pBd09qQXdJaXdpWVdOMGFYWmxJam94TENKMWMyVnlTVVJ6SWpwYkluSmxWRWhKVGtzNkx5OXpaV0poYzNScFlXNHVaMjlsYm1SdlpYSXVibVYwTHlJc0luSmxWRWhKVGtzNkx5OW1ZV05sWW05dmF5NWpiMjB2Wm14MVptWjVNVEl6SWwwc0luSmxkbTlyWldRaU9qQXNJblJwYldWdmRYUWlPaUl5TURJMkxUQTVMVEkwVkRBNE9qSTBPakkzS3pBd09qQXdJaXdpYzJGc2RDSTZJbE53U0hWWWQwVkhkM0pPWTBWalJtOU9VemhMZGpjNVVIbEhSbXg0YVRGMkluMCJ9.MEYCIQCxuGtdM8HqcM0G-PxT7mGKUM6-cMaCiLF4PtT2aGQXRwIhAM6z5t5f1Mpgjw74mXL-NJQ8NcaX_SYTVyGmR8XIVCcF' }
					});

					done();
				}
			},

			addListener: (url, callback) => {
				console.log('addListener: ', url);
				send = callback;
			}
		};

		let proto = activate(protoURL, bus, config).activate;

		send({
			id: 71, type: 'READ', from: "hyperty-runtime://ua.pt/123/graph-connector", to: 'global://registry/',
			body: { guid: 'puZE5qCSGqcjg5mViBM3CdQHKIcHpRoyF3OGLTaYzGs' }
		});
	});

});
