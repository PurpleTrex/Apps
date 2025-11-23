package com.purpletrex.aether.generation;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.purpletrex.aether.entities.ElementType;

/**
 * Procedurally generates sound effects and music
 */
public class SoundGenerator {
    private static final int SAMPLE_RATE = 22050;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Generate Ethereal cry sound
     */
    public void playEtherealCry(int speciesId, ElementType type) {
        // Base frequency based on species ID
        float baseFreq = 200 + (speciesId % 50) * 10;
        
        // Duration
        int duration = 500; // milliseconds
        int numSamples = duration * SAMPLE_RATE / 1000;
        
        // Generate waveform
        short[] samples = new short[numSamples];
        for (int i = 0; i < numSamples; i++) {
            float time = (float)i / SAMPLE_RATE;
            
            // Primary wave (based on element)
            float wave1 = generateWave(time, baseFreq, type);
            
            // Harmonic
            float wave2 = generateWave(time, baseFreq * 1.5f, type) * 0.5f;
            
            // Envelope (ADSR)
            float envelope = getEnvelope(i, numSamples);
            
            samples[i] = (short)((wave1 + wave2) * envelope * Short.MAX_VALUE * 0.5);
        }
        
        // Play sound
        playSound(samples);
    }

    /**
     * Generate capture glyph sound
     */
    public void playCaptureSound(boolean success) {
        int duration = success ? 800 : 400;
        int numSamples = duration * SAMPLE_RATE / 1000;
        short[] samples = new short[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            float time = (float)i / SAMPLE_RATE;
            
            if (success) {
                // Ascending tones for success
                float freq = 440 + (i * 200.0f / numSamples);
                samples[i] = (short)(Math.sin(2 * Math.PI * freq * time) * Short.MAX_VALUE * 0.3);
            } else {
                // Descending tone for failure
                float freq = 440 - (i * 100.0f / numSamples);
                samples[i] = (short)(Math.sin(2 * Math.PI * freq * time) * Short.MAX_VALUE * 0.2);
            }
        }
        
        playSound(samples);
    }

    /**
     * Generate battle hit sound
     */
    public void playHitSound(float effectiveness) {
        int duration = 200;
        int numSamples = duration * SAMPLE_RATE / 1000;
        short[] samples = new short[numSamples];
        
        float freq = effectiveness > 1.0f ? 800 : (effectiveness < 1.0f ? 300 : 500);
        
        for (int i = 0; i < numSamples; i++) {
            float time = (float)i / SAMPLE_RATE;
            float envelope = 1.0f - ((float)i / numSamples);
            
            // Mix of square and noise for impact
            float wave = Math.signum((float)Math.sin(2 * Math.PI * freq * time)) * 0.5f;
            float noise = (float)(Math.random() - 0.5) * 0.3f;
            
            samples[i] = (short)((wave + noise) * envelope * Short.MAX_VALUE * 0.4);
        }
        
        playSound(samples);
    }

    /**
     * Generate menu selection sound
     */
    public void playMenuSound() {
        int duration = 100;
        int numSamples = duration * SAMPLE_RATE / 1000;
        short[] samples = new short[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            float time = (float)i / SAMPLE_RATE;
            samples[i] = (short)(Math.sin(2 * Math.PI * 880 * time) * Short.MAX_VALUE * 0.1);
        }
        
        playSound(samples);
    }

    /**
     * Generate wave based on element type
     */
    private float generateWave(float time, float freq, ElementType type) {
        switch (type) {
            case NATURE:
                return (float)Math.sin(2 * Math.PI * freq * time);
            case FLAME:
                return Math.signum((float)Math.sin(2 * Math.PI * freq * time)); // Square wave
            case AQUA:
                return (float)Math.sin(2 * Math.PI * freq * time) * (float)Math.sin(2 * Math.PI * freq * time * 0.5f);
            case STORM:
                return (float)(Math.random() - 0.5); // Noise
            case VOID:
                return (float)Math.sin(2 * Math.PI * freq * time * 0.5f); // Sub-bass
            case RADIANT:
                // Triangle wave
                float t = (freq * time) % 1.0f;
                return t < 0.5f ? (t * 4 - 1) : (3 - t * 4);
            case MINERAL:
                // Sawtooth
                return 2 * ((freq * time) % 1.0f) - 1;
            case MYSTIC:
                return (float)(Math.sin(2 * Math.PI * freq * time) + Math.sin(2 * Math.PI * freq * 1.5f * time)) * 0.5f;
            default:
                return (float)Math.sin(2 * Math.PI * freq * time);
        }
    }

    /**
     * Get ADSR envelope
     */
    private float getEnvelope(int sample, int totalSamples) {
        float position = (float)sample / totalSamples;
        
        // Attack (0-10%)
        if (position < 0.1f) {
            return position / 0.1f;
        }
        // Decay (10-30%)
        else if (position < 0.3f) {
            return 1.0f - (position - 0.1f) / 0.2f * 0.3f;
        }
        // Sustain (30-70%)
        else if (position < 0.7f) {
            return 0.7f;
        }
        // Release (70-100%)
        else {
            return 0.7f * (1.0f - (position - 0.7f) / 0.3f);
        }
    }

    /**
     * Play generated sound samples
     */
    private void playSound(short[] samples) {
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        
        AudioTrack audioTrack = new AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            Math.max(bufferSize, samples.length * 2),
            AudioTrack.MODE_STATIC
        );
        
        audioTrack.write(samples, 0, samples.length);
        audioTrack.play();
        
        // Clean up after playing
        new Thread(() -> {
            try {
                Thread.sleep(samples.length * 1000 / SAMPLE_RATE + 100);
                audioTrack.stop();
                audioTrack.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
