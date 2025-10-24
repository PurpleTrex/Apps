<?php
// Prevent direct access
header('Content-Type: application/json');

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

// Get the email from POST data
$data = json_decode(file_get_contents('php://input'), true);
$email = isset($data['email']) ? trim($data['email']) : '';

// Validate email
if (empty($email) || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid email address']);
    exit;
}

// File to store emails
$file = '/var/www/html/email-signups.txt';

// Check if email already exists
if (file_exists($file)) {
    $existing = file_get_contents($file);
    if (strpos($existing, $email) !== false) {
        echo json_encode(['success' => false, 'message' => 'Email already registered']);
        exit;
    }
}

// Save email with timestamp
$timestamp = date('Y-m-d H:i:s');
$entry = "$email | $timestamp\n";

if (file_put_contents($file, $entry, FILE_APPEND | LOCK_EX) !== false) {
    echo json_encode(['success' => true, 'message' => 'Thanks! We\'ll notify you when PurpleTalk launches.']);
} else {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to save email. Please try again.']);
}
?>
