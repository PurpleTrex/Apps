// PurpleTalk Matrix Client
  let client = null;
  let currentRoom = null;
  let rooms = [];

  // Initialize on page load
  document.addEventListener('DOMContentLoaded', async () => {
      const token = localStorage.getItem('purpletalk_token');
      const userId = localStorage.getItem('purpletalk_userId');

      if (!token || !userId) {
          window.location.href = 'login.html';
          return;
      }

      await initializeMatrix(token, userId);
      setupEventListeners();
  });

  async function initializeMatrix(token, userId) {
      try {
          // Create Matrix client
          client = matrixcs.createClient({
              baseUrl: 'https://purpletalk.devit.dev',
              accessToken: token,
              userId: userId
          });

          // Set display name in UI
          document.getElementById('userName').textContent = userId.split(':')[0].substring(1);

          // Start the client
          await client.startClient();

          // Setup event listeners
          client.on('Room.timeline', handleNewMessage);
          client.on('RoomState.events', handleRoomStateEvent);
          client.on('sync', handleSync);
          client.on('Room.receipt', handleReceipt);
          client.on('RoomMember.typing', handleTyping);

          // Load rooms
          loadRooms();

      } catch (error) {
          console.error('Failed to initialize Matrix client:', error);
          alert('Failed to connect to server. Please try logging in again.');
          logout();
      }
  }

  function handleSync(state, prevState, data) {
      if (state === 'PREPARED') {
          loadRooms();
      }
  }

  function loadRooms() {
      const roomList = document.getElementById('roomList');
      rooms = client.getRooms();

      if (rooms.length === 0) {
          roomList.innerHTML = '<div class="empty-state" style="padding: 20px; text-align: center; color: #666;">No
  conversations yet. Click "New Chat" to start!</div>';
          return;
      }

      roomList.innerHTML = '';

      rooms.sort((a, b) => {
          const aTime = a.getLastActiveTimestamp();
          const bTime = b.getLastActiveTimestamp();
          return bTime - aTime;
      });

      rooms.forEach(room => {
          const roomItem = createRoomElement(room);
          roomList.appendChild(roomItem);
      });
  }

  function createRoomElement(room) {
      const div = document.createElement('div');
      div.className = 'room-item';
      div.dataset.roomId = room.roomId;

      const timeline = room.getLiveTimeline();
      const events = timeline.getEvents();
      const lastMessage = events[events.length - 1];

      const members = room.getJoinedMembers();
      const otherMember = members.find(m => m.userId !== client.getUserId());
      const displayName = otherMember ? getDisplayName(otherMember.userId) : room.name || 'Unknown';

      const lastMessageText = lastMessage ?
          (lastMessage.getContent().body || 'Media message') :
          'No messages yet';

      const lastMessageTime = lastMessage ?
          formatTime(lastMessage.getTs()) : '';

      // Count unread messages
      const unreadCount = room.getUnreadNotificationCount();

      div.innerHTML = `
          <div class="room-avatar">${getInitials(displayName)}</div>
          <div class="room-info">
              <div class="room-name">${escapeHtml(displayName)}</div>
              <div class="room-last-message">${escapeHtml(lastMessageText)}</div>
          </div>
          <div class="room-meta">
              <div class="room-time">${lastMessageTime}</div>
              ${unreadCount > 0 ? `<div class="unread-badge">${unreadCount}</div>` : ''}
          </div>
      `;

      div.addEventListener('click', () => selectRoom(room));

      return div;
  }

  function selectRoom(room) {
      currentRoom = room;

      // Update active state
      document.querySelectorAll('.room-item').forEach(item => {
          item.classList.remove('active');
      });
      document.querySelector(`[data-room-id="${room.roomId}"]`).classList.add('active');

      // Show chat area
      document.getElementById('chatHeader').style.display = 'flex';
      document.getElementById('messageInputContainer').style.display = 'block';

      // Update header
      const members = room.getJoinedMembers();
      const otherMember = members.find(m => m.userId !== client.getUserId());
      const displayName = otherMember ? getDisplayName(otherMember.userId) : room.name || 'Unknown';

      document.getElementById('chatName').textContent = displayName;
      document.getElementById('chatStatus').textContent = members.length + ' member(s)';

      // Load messages
      loadMessages(room);

      // Mark as read
      client.sendReadReceipt(null, room.getLiveTimeline().getEvents().slice(-1)[0]);
  }

  function loadMessages(room) {
      const container = document.getElementById('messagesContainer');
      container.innerHTML = '';

      const timeline = room.getLiveTimeline();
      const events = timeline.getEvents();

      let lastDate = null;

      events.forEach(event => {
          if (event.getType() === 'm.room.message') {
              const messageDate = new Date(event.getTs()).toLocaleDateString();

              // Add date separator if needed
              if (lastDate !== messageDate) {
                  const separator = document.createElement('div');
                  separator.className = 'date-separator';
                  separator.innerHTML = `<span>${messageDate}</span>`;
                  container.appendChild(separator);
                  lastDate = messageDate;
              }

              const messageEl = createMessageElement(event);
              container.appendChild(messageEl);
          }
      });

      // Scroll to bottom
      container.scrollTop = container.scrollHeight;
  }

  function createMessageElement(event) {
      const div = document.createElement('div');
      const isSent = event.getSender() === client.getUserId();
      div.className = `message ${isSent ? 'sent' : 'received'}`;

      const content = event.getContent();
      let messageHtml = '';

      if (content.msgtype === 'm.text') {
          messageHtml = escapeHtml(content.body);
      } else if (content.msgtype === 'm.image') {
          messageHtml = `<img src="${content.url}" class="message-image" alt="Image">`;
      } else if (content.msgtype === 'm.file') {
          messageHtml = `
              <a href="${content.url}" class="message-file" download>
                  <span class="file-icon">📎</span>
                  <div class="file-info">
                      <div class="file-name">${escapeHtml(content.body)}</div>
                      <div class="file-size">${formatFileSize(content.info?.size || 0)}</div>
                  </div>
              </a>
          `;
      }

      const time = formatTime(event.getTs());
      const status = isSent ? getMessageStatus(event) : '';

      div.innerHTML = `
          <div class="message-bubble">
              ${messageHtml}
              <div class="message-time">${time}${status}</div>
          </div>
      `;

      return div;
  }

  function handleNewMessage(event, room, toStartOfTimeline) {
      if (!toStartOfTimeline && room === currentRoom && event.getType() === 'm.room.message') {
          const container = document.getElementById('messagesContainer');
          const messageEl = createMessageElement(event);
          container.appendChild(messageEl);
          container.scrollTop = container.scrollHeight;

          // Update room list
          loadRooms();

          // Send read receipt if not our message
          if (event.getSender() !== client.getUserId()) {
              client.sendReadReceipt(event, {});
          }
      }
  }

  function handleTyping(event, member) {
      if (currentRoom && member.roomId === currentRoom.roomId) {
          const typingUsers = currentRoom.getMembersWithMembership('join')
              .filter(m => m.typing && m.userId !== client.getUserId());

          const indicator = document.getElementById('typingIndicator');
          const typingText = document.getElementById('typingText');

          if (typingUsers.length > 0) {
              const names = typingUsers.map(u => getDisplayName(u.userId));
              typingText.textContent = names.join(', ') + ' typing...';
              indicator.style.display = 'block';
          } else {
              indicator.style.display = 'none';
          }
      }
  }

  function handleReceipt(event, room) {
      if (room === currentRoom) {
          // Update read receipts in UI
          loadMessages(room);
      }
  }

  function handleRoomStateEvent(event, state) {
      // Handle room state changes
      loadRooms();
  }

  // Send message
  async function sendMessage() {
      if (!currentRoom) return;

      const input = document.getElementById('messageInput');
      const message = input.value.trim();

      if (!message) return;

      try {
          await client.sendMessage(currentRoom.roomId, {
              msgtype: 'm.text',
              body: message
          });

          input.value = '';
      } catch (error) {
          console.error('Failed to send message:', error);
          alert('Failed to send message');
      }
  }

  // Create new chat
  async function createNewChat() {
      const input = document.getElementById('newChatInput').value.trim();
      if (!input) return;

      try {
          let targetUserId = input;

          // If it's a phone number, convert to Matrix ID
          if (input.match(/^\+?[0-9]+$/)) {
              const phoneDigits = input.replace(/[^0-9]/g, '');
              const salt = 'PurpleTalk2025_purpletalk.devit.dev';
              const hash = await sha256(salt + phoneDigits);
              const username = 'tel_' + hash.substring(0, 16);
              targetUserId = '@' + username + ':purpletalk.devit.dev';
          }

          // Create direct message room
          const room = await client.createRoom({
              preset: 'trusted_private_chat',
              invite: [targetUserId],
              is_direct: true
          });

          // Close modal
          document.getElementById('newChatModal').style.display = 'none';
          document.getElementById('newChatInput').value = '';

          // Refresh rooms
          setTimeout(() => loadRooms(), 1000);

      } catch (error) {
          console.error('Failed to create chat:', error);
          alert('Failed to create chat. User may not exist.');
      }
  }

  // Event Listeners
  function setupEventListeners() {
      // Send button
      document.getElementById('sendBtn').addEventListener('click', sendMessage);

      // Enter key to send
      document.getElementById('messageInput').addEventListener('keypress', (e) => {
          if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              sendMessage();
          }
      });

      // Typing indicator
      let typingTimer;
      document.getElementById('messageInput').addEventListener('input', () => {
          if (currentRoom) {
              client.sendTyping(currentRoom.roomId, true, 5000);
              clearTimeout(typingTimer);
              typingTimer = setTimeout(() => {
                  client.sendTyping(currentRoom.roomId, false);
              }, 5000);
          }
      });

      // New chat modal
      document.getElementById('newChatBtn').addEventListener('click', () => {
          document.getElementById('newChatModal').style.display = 'flex';
      });

      document.getElementById('closeModalBtn').addEventListener('click', () => {
          document.getElementById('newChatModal').style.display = 'none';
      });

      document.getElementById('cancelNewChatBtn').addEventListener('click', () => {
          document.getElementById('newChatModal').style.display = 'none';
      });

      document.getElementById('startChatBtn').addEventListener('click', createNewChat);

      // File attachment
      document.querySelector('.btn-attach').addEventListener('click', () => {
          document.getElementById('fileInput').click();
      });

      document.getElementById('fileInput').addEventListener('change', async (e) => {
          const file = e.target.files[0];
          if (file && currentRoom) {
              await uploadFile(file);
          }
      });

      // Logout
      document.getElementById('logoutBtn').addEventListener('click', logout);

      // Search
      document.getElementById('searchInput').addEventListener('input', (e) => {
          const query = e.target.value.toLowerCase();
          document.querySelectorAll('.room-item').forEach(item => {
              const name = item.querySelector('.room-name').textContent.toLowerCase();
              item.style.display = name.includes(query) ? 'flex' : 'none';
          });
      });
  }

  // File upload
  async function uploadFile(file) {
      try {
          const uploadResponse = await client.uploadContent(file, {
              name: file.name,
              type: file.type
          });

          const content = {
              body: file.name,
              info: {
                  size: file.size,
                  mimetype: file.type
              },
              url: uploadResponse.content_uri
          };

          if (file.type.startsWith('image/')) {
              content.msgtype = 'm.image';
          } else {
              content.msgtype = 'm.file';
          }

          await client.sendMessage(currentRoom.roomId, content);

      } catch (error) {
          console.error('Failed to upload file:', error);
          alert('Failed to upload file');
      }
  }

  // Utility functions
  function escapeHtml(text) {
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
  }

  function getInitials(name) {
      if (name.startsWith('@tel_')) {
          return '👤';
      }
      const parts = name.split(' ');
      if (parts.length >= 2) {
          return parts[0][0] + parts[1][0];
      }
      return name.substring(0, 2).toUpperCase();
  }

  function getDisplayName(userId) {
      if (userId.startsWith('@tel_')) {
          return 'User ' + userId.substring(5, 9);
      }
      return userId.split(':')[0].substring(1);
  }

  function formatTime(timestamp) {
      const date = new Date(timestamp);
      const now = new Date();

      if (date.toDateString() === now.toDateString()) {
          return date.toLocaleTimeString('en-US', {
              hour: 'numeric',
              minute: '2-digit',
              hour12: true
          });
      } else {
          return date.toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric'
          });
      }
  }

  function formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  function getMessageStatus(event) {
      // This would check read receipts
      return ' ✓✓'; // For now, show as delivered
  }

  async function sha256(message) {
      const msgBuffer = new TextEncoder().encode(message);
      const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  }

  function logout() {
      if (confirm('Are you sure you want to logout?')) {
          if (client) {
              client.stopClient();
          }
          localStorage.clear();
          window.location.href = 'login.html';
      }
  }
