<?php
/**
 * PurpleTalk API Endpoints
 * Handles account actions and Matrix operations
 */

// Enable CORS
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Content-Type: application/json');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Matrix server configuration
define('MATRIX_SERVER', 'https://purpletalk.devit.dev');
define('MATRIX_DOMAIN', 'purpletalk.devit.dev');

// Get JSON input and Authorization header
$input = json_decode(file_get_contents('php://input'), true);
$authHeader = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
$accessToken = str_replace('Bearer ', '', $authHeader);

if (!$input || !isset($input['action'])) {
    sendError('Invalid request');
}

$action = $input['action'];

// Route to appropriate action
switch ($action) {
    case 'getUserInfo':
        getUserInfo($accessToken, $input);
        break;
    case 'getProfile':
        getProfile($accessToken, $input);
        break;
    case 'updateProfile':
        updateProfile($accessToken, $input);
        break;
    case 'changePassword':
        changePassword($accessToken, $input);
        break;
    case 'deleteAccount':
        deleteAccount($accessToken);
        break;
    case 'getRooms':
        getRooms($accessToken);
        break;
    case 'sendMessage':
        sendMessage($accessToken, $input);
        break;
    default:
        sendError('Unknown action');
}

/**
 * Get user information including rooms and devices
 */
function getUserInfo($accessToken, $input) {
    if (!$accessToken) {
        sendError('Authentication required', 401);
    }

    // Get joined rooms
    $roomsResponse = matrixRequest('/_matrix/client/v3/joined_rooms', 'GET', null, $accessToken);

    $roomCount = 0;
    if ($roomsResponse && isset($roomsResponse['joined_rooms'])) {
        $roomCount = count($roomsResponse['joined_rooms']);
    }

    sendSuccess('User info retrieved', [
        'roomCount' => $roomCount,
        'deviceCount' => 1 // Simplified - could query devices endpoint
    ]);
}

/**
 * Get user profile
 */
function getProfile($accessToken, $input) {
    if (!$accessToken || !isset($input['userId'])) {
        sendError('Authentication and userId required', 401);
    }

    $userId = $input['userId'];

    // Get profile from Matrix
    $profile = matrixRequest('/_matrix/client/v3/profile/' . urlencode($userId), 'GET', null, $accessToken);

    if ($profile && isset($profile['displayname'])) {
        sendSuccess('Profile retrieved', [
            'displayName' => $profile['displayname'],
            'avatarUrl' => $profile['avatar_url'] ?? null
        ]);
    } else {
        sendSuccess('Profile retrieved', [
            'displayName' => null,
            'avatarUrl' => null
        ]);
    }
}

/**
 * Update user profile (display name)
 */
function updateProfile($accessToken, $input) {
    if (!$accessToken || !isset($input['displayName'])) {
        sendError('Authentication and displayName required', 401);
    }

    // Get user ID from access token (need to parse or get from session)
    // For simplicity, we'll get it from a whoami endpoint
    $whoami = matrixRequest('/_matrix/client/v3/account/whoami', 'GET', null, $accessToken);

    if (!$whoami || !isset($whoami['user_id'])) {
        sendError('Failed to get user ID');
    }

    $userId = $whoami['user_id'];
    $displayName = $input['displayName'];

    // Update display name
    $response = matrixRequest(
        '/_matrix/client/v3/profile/' . urlencode($userId) . '/displayname',
        'PUT',
        ['displayname' => $displayName],
        $accessToken
    );

    if ($response !== null) {
        sendSuccess('Profile updated successfully');
    } else {
        sendError('Failed to update profile');
    }
}

/**
 * Change user password
 */
function changePassword($accessToken, $input) {
    if (!$accessToken || !isset($input['currentPassword']) || !isset($input['newPassword'])) {
        sendError('Authentication, current password and new password required', 401);
    }

    $currentPassword = $input['currentPassword'];
    $newPassword = $input['newPassword'];

    // Validate new password length
    if (strlen($newPassword) < 8) {
        sendError('New password must be at least 8 characters');
    }

    // Change password on Matrix server
    $requestBody = [
        'new_password' => $newPassword,
        'auth' => [
            'type' => 'm.login.password',
            'password' => $currentPassword
        ]
    ];

    $response = matrixRequest('/_matrix/client/v3/account/password', 'POST', $requestBody, $accessToken);

    if ($response !== null && !isset($response['errcode'])) {
        sendSuccess('Password changed successfully');
    } else {
        $errorMessage = 'Failed to change password';

        if (isset($response['error'])) {
            $errorMessage = $response['error'];
        }

        if (isset($response['errcode']) && $response['errcode'] === 'M_FORBIDDEN') {
            $errorMessage = 'Current password is incorrect';
        }

        sendError($errorMessage);
    }
}

/**
 * Delete user account (deactivate)
 */
function deleteAccount($accessToken) {
    if (!$accessToken) {
        sendError('Authentication required', 401);
    }

    // Deactivate account on Matrix server
    // Note: This requires auth, typically password
    $response = matrixRequest('/_matrix/client/v3/account/deactivate', 'POST', [], $accessToken);

    if ($response !== null && !isset($response['errcode'])) {
        sendSuccess('Account deleted successfully');
    } else {
        $errorMessage = 'Failed to delete account';

        if (isset($response['error'])) {
            $errorMessage = $response['error'];
        }

        sendError($errorMessage);
    }
}

/**
 * Get user's joined rooms
 */
function getRooms($accessToken) {
    if (!$accessToken) {
        sendError('Authentication required', 401);
    }

    $roomsResponse = matrixRequest('/_matrix/client/v3/joined_rooms', 'GET', null, $accessToken);

    if ($roomsResponse && isset($roomsResponse['joined_rooms'])) {
        sendSuccess('Rooms retrieved', [
            'rooms' => $roomsResponse['joined_rooms']
        ]);
    } else {
        sendError('Failed to get rooms');
    }
}

/**
 * Send message to a room
 */
function sendMessage($accessToken, $input) {
    if (!$accessToken || !isset($input['roomId']) || !isset($input['message'])) {
        sendError('Authentication, roomId and message required', 401);
    }

    $roomId = $input['roomId'];
    $message = $input['message'];
    $txnId = uniqid('web_', true);

    $messageBody = [
        'msgtype' => 'm.text',
        'body' => $message
    ];

    $endpoint = sprintf(
        '/_matrix/client/v3/rooms/%s/send/m.room.message/%s',
        urlencode($roomId),
        urlencode($txnId)
    );

    $response = matrixRequest($endpoint, 'PUT', $messageBody, $accessToken);

    if ($response && isset($response['event_id'])) {
        sendSuccess('Message sent', [
            'eventId' => $response['event_id']
        ]);
    } else {
        sendError('Failed to send message');
    }
}

/**
 * Make HTTP request to Matrix server
 */
function matrixRequest($endpoint, $method = 'GET', $data = null, $accessToken = null) {
    $url = MATRIX_SERVER . $endpoint;

    $ch = curl_init($url);

    // Set request method
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);

    // Set headers
    $headers = ['Content-Type: application/json'];
    if ($accessToken) {
        $headers[] = 'Authorization: Bearer ' . $accessToken;
    }
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

    // Set request body for POST/PUT
    if ($data !== null && ($method === 'POST' || $method === 'PUT')) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    }

    // Return response as string
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    // SSL verification
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);

    // Set timeout
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);

    // Execute request
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if (curl_errno($ch)) {
        $error = curl_error($ch);
        curl_close($ch);
        error_log("Matrix API Error: " . $error);
        return null;
    }

    curl_close($ch);

    // Parse JSON response
    $responseData = json_decode($response, true);

    return $responseData;
}

/**
 * Send success response
 */
function sendSuccess($message, $data = []) {
    echo json_encode([
        'success' => true,
        'message' => $message,
        ...$data
    ]);
    exit();
}

/**
 * Send error response
 */
function sendError($message, $code = 400) {
    http_response_code($code);
    echo json_encode([
        'success' => false,
        'message' => $message
    ]);
    exit();
}
?>
