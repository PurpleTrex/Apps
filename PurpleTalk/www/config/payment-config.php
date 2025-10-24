<?php
  // NEVER put this file in the web-accessible directory!
  return [
      'paypal' => [
          'mode' => 'live', // Change to 'sandbox' for testing
          'live' => [
              'client_id' => 'AWixlpP3ptuQM-FsN2XuyIdtMMlw5b1yKpFJmS_TVfBAv2B35TWwwIQ5Yp-w_wBKGh3syJXA25MnTdPN',
              'secret' => 'ELR16G-3nMq9O-AAQws4JS-RV2gC5z97TFdkCoY9uRDz8JvZwRnDyijJN5sNTHzZmEjbCvTM5BWEe4xr'
          ],
          'sandbox' => [
              'username' => 'sb-smxsz47141462@business.example.com',
              'password' => 'x}7jAN--'
          ]
      ],
      'crypto' => [
          'bitcoin' => 'bc1qlfax9cl6z246q2ht4hfw6l35fdmts5dmqc6ysa',
          'ethereum' => '0xE2bB859B4BBDE94e8A8410b980E0fBC3F9296E92',
          'solana' => '32LUmqP7hR4acDcgYECKE1gD3Miw6iJ4Yoxvi1kZXLa7',
          'dogecoin' => 'DSWdDWHsZgSzkhgTeBeGVHwKVQeRCZ13mX'
      ]
  ];
  ?>
