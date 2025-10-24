<?php
  // Load config from secure location
  $config = include('/var/www/config/payment-config.php');

  header('Content-Type: application/json');
  header('Access-Control-Allow-Origin: *');
  header('Access-Control-Allow-Methods: POST');
  header('Access-Control-Allow-Headers: Content-Type');

  if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
      http_response_code(405);
      echo json_encode(['error' => 'Method not allowed']);
      exit;
  }

  $input = json_decode(file_get_contents('php://input'), true);

  // Log donation to file (optional)
  $logFile = '/var/www/data/donations.log';
  $logEntry = date('Y-m-d H:i:s') . ' | ' . json_encode($input) . PHP_EOL;
  file_put_contents($logFile, $logEntry, FILE_APPEND | LOCK_EX);

  // Send notification email (optional)
  $to = 'your-email@example.com';
  $subject = 'New PurpleTalk Donation';
  $message = "New donation received!\n\n";
  $message .= "Type: " . $input['type'] . "\n";
  $message .= "Amount: $" . $input['amount'] . "\n";
  if (isset($input['transaction_id'])) {
      $message .= "Transaction ID: " . $input['transaction_id'] . "\n";
  }
  $headers = 'From: donations@purpletalk.devit.dev';

  mail($to, $subject, $message, $headers);

  echo json_encode(['success' => true]);
  ?>
