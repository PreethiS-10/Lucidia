#!/usr/bin/env python3
"""
Dream Visualizer - Using FREE Pollinations.ai (No API Key Required)
"""

import sys
import json
import os
import argparse
import uuid
from datetime import datetime
import requests
import urllib.parse
from PIL import Image
import io

class DreamVisualizerFree:
    def __init__(self):
        self.base_url = "https://image.pollinations.ai/prompt"

        # Emotion to style mapping
        self.emotion_styles = {
            "joy": "bright, golden light, uplifting, celebration, warm colors",
            "fear": "dark, shadows, dramatic lighting, ominous, mysterious",
            "anxiety": "chaotic, swirling, restless energy, tension",
            "sadness": "melancholic, blue tones, gentle rain, soft light",
            "peace": "serene, calm, soft pastels, zen, harmonious",
            "excitement": "dynamic, energetic, vibrant, explosive colors",
            "confusion": "surreal, fragmented, abstract, maze-like"
        }

    def enhance_dream_prompt(self, dream_text, emotions, symbols):
        """Create enhanced prompt for image generation"""
        # Base artistic prompt
        base_prompt = f"surreal dreamscape artwork: {dream_text[:100]}"

        # Add style elements
        style_elements = ["digital art", "ethereal", "mystical", "vibrant colors", "dreamlike", "high quality"]

        # Add emotion-based styling
        if emotions:
            dominant_emotion = max(emotions, key=emotions.get)
            if dominant_emotion in self.emotion_styles:
                style_elements.append(self.emotion_styles[dominant_emotion])

        # Add symbols
        if symbols:
            # Add up to 2 symbols to avoid prompt overload
            style_elements.extend(symbols[:2])

        # Create final prompt
        prompt = f"{base_prompt}, {', '.join(style_elements)}"

        # Keep prompt under 500 characters for best results
        if len(prompt) > 500:
            prompt = prompt[:497] + "..."

        return prompt

    def generate_dream_image(self, dream_text, emotions=None, symbols=None):
        """Generate image using Pollinations.ai - completely free, no API key needed"""
        try:
            print("GENERATING: Using Pollinations.ai (free, no API key needed)...", file=sys.stderr)

            # Create enhanced prompt
            enhanced_prompt = self.enhance_dream_prompt(dream_text, emotions or {}, symbols or [])
            print(f"PROMPT: {enhanced_prompt}", file=sys.stderr)

            # URL encode the prompt
            encoded_prompt = urllib.parse.quote(enhanced_prompt)

            # Build URL with parameters for better quality
            image_url = f"{self.base_url}/{encoded_prompt}"
            params = {
                "width": "512",
                "height": "512",
                "model": "flux",  # Use FLUX model for better quality
                "enhance": "true"
            }

            # Add parameters to URL
            param_string = "&".join([f"{k}={v}" for k, v in params.items()])
            full_url = f"{image_url}?{param_string}"

            print("PROCESSING: Generating image...", file=sys.stderr)

            # Make request - no API key needed!
            response = requests.get(full_url, timeout=60)

            if response.status_code == 200:
                print("DOWNLOADING: Image generated successfully", file=sys.stderr)
                try:
                    image = Image.open(io.BytesIO(response.content))
                    print("GENERATED: Free AI image completed", file=sys.stderr)
                    return image
                except Exception as e:
                    print(f"ERROR_IMAGE: Could not process image: {e}", file=sys.stderr)
                    return None
            else:
                print(f"ERROR_API: Failed to generate image: HTTP {response.status_code}", file=sys.stderr)
                print(f"Response: {response.text[:200]}", file=sys.stderr)
                return None

        except requests.exceptions.Timeout:
            print("ERROR_TIMEOUT: Request timed out (try again)", file=sys.stderr)
            return None
        except Exception as e:
            print(f"ERROR_GENERATION: {str(e)}", file=sys.stderr)
            return None

    def save_image(self, image, output_dir="generated_images"):
        """Save generated image with unique filename"""
        if not image:
            return None

        try:
            # Create output directory
            os.makedirs(output_dir, exist_ok=True)

            # Generate unique filename
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            unique_id = str(uuid.uuid4())[:8]
            filename = f"dream_free_{timestamp}_{unique_id}.png"
            filepath = os.path.join(output_dir, filename)

            # Save image
            image.save(filepath, "PNG")
            print(f"SAVED: Image saved to {filepath}", file=sys.stderr)
            return filepath

        except Exception as e:
            print(f"ERROR_SAVE: {str(e)}", file=sys.stderr)
            return None

def main():
    parser = argparse.ArgumentParser(description="Generate dream visualization using FREE AI (no API key)")
    parser.add_argument("--dream-text", help="Dream description text")  # Make this optional now
    parser.add_argument("--emotions", help="JSON string of emotions dict")
    parser.add_argument("--symbols", help="JSON string of symbols list")
    parser.add_argument("--param-file", help="JSON parameter file")  # ADD THIS LINE!
    parser.add_argument("--output-dir", default="generated_images", help="Output directory")

    args = parser.parse_args()

    # Handle parameter file input (preferred method)
    if args.param_file:
        try:
            print(f"LOADING: Reading parameters from {args.param_file}", file=sys.stderr)
            with open(args.param_file, 'r', encoding='utf-8') as f:
                params = json.load(f)
            dream_text = params.get('dream_text', '')
            emotions = params.get('emotions', {})
            symbols = params.get('symbols', [])
            print(f"LOADED: Dream text length: {len(dream_text)}", file=sys.stderr)
        except Exception as e:
            print(f"ERROR_PARAM_FILE: {str(e)}", file=sys.stderr)
            return 1
    else:
        # Handle command line arguments (fallback)
        if not args.dream_text:
            print("ERROR_INPUT: Either --dream-text or --param-file is required", file=sys.stderr)
            return 1

        dream_text = args.dream_text
        emotions = {}
        symbols = []

        try:
            if args.emotions:
                emotions = json.loads(args.emotions)
            if args.symbols:
                symbols = json.loads(args.symbols)
        except json.JSONDecodeError as e:
            print(f"ERROR_JSON: {str(e)}", file=sys.stderr)
            return 1

    # Validate dream text
    if not dream_text.strip():
        print("ERROR_INPUT: Dream text is empty", file=sys.stderr)
        return 1

    # Create visualizer
    visualizer = DreamVisualizerFree()

    print("STARTING: FREE AI dream visualization (no API key required)", file=sys.stderr)
    image = visualizer.generate_dream_image(dream_text, emotions, symbols)

    if image:
        filepath = visualizer.save_image(image, args.output_dir)
        if filepath:
            print(f"SUCCESS: {os.path.abspath(filepath)}")
            return 0
        else:
            print("ERROR_SAVE: Failed to save image", file=sys.stderr)
            return 1
    else:
        print("ERROR_GENERATION: Failed to generate image", file=sys.stderr)
        return 1

if __name__ == "__main__":
    sys.exit(main())
