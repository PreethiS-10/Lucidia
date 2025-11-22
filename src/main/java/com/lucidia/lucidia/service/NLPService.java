package com.lucidia.lucidia.service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NLPService {

    // Enhanced emotion lexicon with weights and synonyms
    private static final Map<String, EmotionPattern> EMOTION_LEXICON = createEmotionLexicon();

    // Advanced dream symbol database with contextual meanings
    private static final Map<String, SymbolMeaning> SYMBOL_DATABASE = createSymbolDatabase();

    // Linguistic patterns for dream analysis
    private static final Map<String, Pattern> LINGUISTIC_PATTERNS = createLinguisticPatterns();

    public NLPService() {
        // Initialize any required NLP components
    }

    // COMPATIBLE METHOD: Returns List<String> for backward compatibility
    public List<String> extractSymbols(String dreamText) {
        List<DreamSymbol> dreamSymbols = extractSymbolsEnhanced(dreamText);
        return dreamSymbols.stream()
                .map(DreamSymbol::getName)
                .collect(Collectors.toList());
    }

    // ENHANCED METHOD: Returns detailed DreamSymbol objects for advanced analysis
    public List<DreamSymbol> extractSymbolsEnhanced(String dreamText) {
        String normalizedText = preprocessText(dreamText);
        List<DreamSymbol> symbols = new ArrayList<>();

        symbols.addAll(extractDirectSymbols(normalizedText));
        symbols.addAll(extractContextualSymbols(normalizedText));
        symbols.addAll(extractMetaphoricalSymbols(normalizedText));

        return rankAndFilterSymbols(symbols);
    }

    // COMPATIBLE METHOD: Simple interpretation for backward compatibility
    public String generateInterpretation(Map<String, Double> emotions, List<String> symbols) {
        StringBuilder interpretation = new StringBuilder();

        // Find dominant emotion
        String dominantEmotion = emotions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");

        interpretation.append("Based on your dream analysis:\n\n");
        interpretation.append("Primary Emotional Tone: ").append(capitalize(dominantEmotion)).append("\n");

        if (!symbols.isEmpty()) {
            interpretation.append("Key Symbols: ").append(String.join(", ", symbols)).append("\n\n");
        }

        // Basic interpretations
        switch (dominantEmotion) {
            case "fear" -> interpretation.append("This dream may reflect underlying anxieties or concerns in your waking life. The presence of fear suggests you're processing challenges or uncertainties.");
            case "joy" -> interpretation.append("This dream indicates positive emotional processing and mental well-being. Joyful dreams often reflect contentment and optimism.");
            case "anxiety" -> interpretation.append("This dream may indicate stress or unresolved issues that need attention. Consider what might be causing tension in your daily life.");
            case "sadness" -> interpretation.append("This dream could reflect feelings of loss or melancholy. It may be helping you process emotional healing.");
            case "peace" -> interpretation.append("This dream suggests inner tranquility and emotional balance. Peaceful dreams often indicate psychological harmony.");
            case "excitement" -> interpretation.append("This dream reflects energetic and dynamic emotional states. You may be anticipating positive changes.");
            case "confusion" -> interpretation.append("This dream suggests you're processing complex situations or decisions. Clarity may emerge with reflection.");
            default -> interpretation.append("This dream shows varied emotional processing with multiple themes present.");
        }

        return interpretation.toString();
    }

    // ENHANCED METHOD: Comprehensive dream interpretation generator
    public DreamInterpretation generateEnhancedInterpretation(String dreamText) {
        Map<String, Double> emotions = analyzeEmotion(dreamText);
        List<DreamSymbol> symbols = extractSymbolsEnhanced(dreamText);

        DreamInterpretation interpretation = new DreamInterpretation();

        interpretation.setEmotionalProfile(analyzeEmotionalProfile(emotions));
        interpretation.setSymbolicMeanings(analyzeSymbolicMeanings(symbols));
        interpretation.setNarrativeStructure(analyzeNarrativeStructure(dreamText));
        interpretation.setPsychologicalThemes(identifyPsychologicalThemes(dreamText, emotions, symbols));
        interpretation.setPersonalInsights(generatePersonalInsights(dreamText, emotions, symbols));

        return interpretation;
    }

    // Emotion analysis (unchanged - already returns compatible Map<String, Double>)
    public Map<String, Double> analyzeEmotion(String dreamText) {
        Map<String, Double> emotionScores = new HashMap<>();
        String normalizedText = preprocessText(dreamText);

        // Initialize all emotion categories
        for (String emotion : EMOTION_LEXICON.keySet()) {
            emotionScores.put(emotion, 0.0);
        }

        analyzeWordLevelEmotions(normalizedText, emotionScores);
        analyzePhraseLevelEmotions(normalizedText, emotionScores);
        analyzeContextualEmotions(normalizedText, emotionScores);

        normalizeEmotionScores(emotionScores);
        applyIntensityModifiers(normalizedText, emotionScores);

        return emotionScores;
    }

    // ========== PRIVATE IMPLEMENTATION METHODS ==========

    private String preprocessText(String text) {
        if (text == null) return "";

        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void analyzeWordLevelEmotions(String text, Map<String, Double> emotionScores) {
        String[] words = text.split("\\s+");

        for (String word : words) {
            for (Map.Entry<String, EmotionPattern> entry : EMOTION_LEXICON.entrySet()) {
                EmotionPattern pattern = entry.getValue();
                if (pattern.matches(word)) {
                    double currentScore = emotionScores.get(entry.getKey());
                    emotionScores.put(entry.getKey(), currentScore + pattern.getWeight());
                }
            }
        }
    }

    private void analyzePhraseLevelEmotions(String text, Map<String, Double> emotionScores) {
        for (Map.Entry<String, EmotionPattern> entry : EMOTION_LEXICON.entrySet()) {
            EmotionPattern pattern = entry.getValue();
            for (String phrase : pattern.getPhrases()) {
                if (text.contains(phrase)) {
                    double currentScore = emotionScores.get(entry.getKey());
                    emotionScores.put(entry.getKey(), currentScore + pattern.getPhraseWeight());
                }
            }
        }
    }

    private void analyzeContextualEmotions(String text, Map<String, Double> emotionScores) {
        for (Map.Entry<String, Pattern> entry : LINGUISTIC_PATTERNS.entrySet()) {
            String emotion = entry.getKey();
            Pattern pattern = entry.getValue();

            long matches = pattern.matcher(text).results().count();
            if (matches > 0) {
                double currentScore = emotionScores.get(emotion);
                emotionScores.put(emotion, currentScore + (matches * 0.1));
            }
        }
    }

    private List<DreamSymbol> extractDirectSymbols(String text) {
        List<DreamSymbol> symbols = new ArrayList<>();

        for (Map.Entry<String, SymbolMeaning> entry : SYMBOL_DATABASE.entrySet()) {
            String symbol = entry.getKey();
            SymbolMeaning meaning = entry.getValue();

            if (text.contains(symbol)) {
                DreamSymbol dreamSymbol = new DreamSymbol(symbol, meaning);
                dreamSymbol.setConfidence(0.8);
                symbols.add(dreamSymbol);
            }
        }

        return symbols;
    }

    private List<DreamSymbol> extractContextualSymbols(String text) {
        List<DreamSymbol> symbols = new ArrayList<>();

        for (Map.Entry<String, SymbolMeaning> entry : SYMBOL_DATABASE.entrySet()) {
            String symbol = entry.getKey();
            SymbolMeaning meaning = entry.getValue();

            for (String contextClue : meaning.getContextClues()) {
                if (text.contains(contextClue)) {
                    DreamSymbol dreamSymbol = new DreamSymbol(symbol, meaning);
                    dreamSymbol.setConfidence(0.6);
                    symbols.add(dreamSymbol);
                    break;
                }
            }
        }

        return symbols;
    }

    private List<DreamSymbol> extractMetaphoricalSymbols(String text) {
        List<DreamSymbol> symbols = new ArrayList<>();

        String[] sentences = text.split("\\.");
        for (String sentence : sentences) {
            for (Map.Entry<String, SymbolMeaning> entry : SYMBOL_DATABASE.entrySet()) {
                String symbol = entry.getKey();
                SymbolMeaning meaning = entry.getValue();

                for (String metaphor : meaning.getMetaphors()) {
                    if (sentence.contains(metaphor)) {
                        DreamSymbol dreamSymbol = new DreamSymbol(symbol, meaning);
                        dreamSymbol.setConfidence(0.5);
                        symbols.add(dreamSymbol);
                    }
                }
            }
        }

        return symbols;
    }

    private List<DreamSymbol> rankAndFilterSymbols(List<DreamSymbol> symbols) {
        Map<String, DreamSymbol> bestSymbols = new HashMap<>();

        for (DreamSymbol symbol : symbols) {
            String key = symbol.getName();
            if (!bestSymbols.containsKey(key) || symbol.getConfidence() > bestSymbols.get(key).getConfidence()) {
                bestSymbols.put(key, symbol);
            }
        }

        return bestSymbols.values().stream()
                .sorted((s1, s2) -> Double.compare(s2.getConfidence(), s1.getConfidence()))
                .limit(8)
                .collect(Collectors.toList());
    }

    private void normalizeEmotionScores(Map<String, Double> emotionScores) {
        double maxScore = emotionScores.values().stream().max(Double::compare).orElse(1.0);

        if (maxScore > 0) {
            for (String emotion : emotionScores.keySet()) {
                double normalizedScore = emotionScores.get(emotion) / maxScore;
                emotionScores.put(emotion, Math.min(1.0, normalizedScore));
            }
        }
    }

    private void applyIntensityModifiers(String text, Map<String, Double> emotionScores) {
        Pattern intensifiers = Pattern.compile("\\b(very|extremely|incredibly|absolutely|terribly)\\b");
        long intensityCount = intensifiers.matcher(text).results().count();

        Pattern diminishers = Pattern.compile("\\b(slightly|somewhat|a bit|kind of|sort of)\\b");
        long diminishCount = diminishers.matcher(text).results().count();

        double intensityFactor = 1.0 + (intensityCount * 0.2) - (diminishCount * 0.15);

        for (String emotion : emotionScores.keySet()) {
            double score = emotionScores.get(emotion);
            emotionScores.put(emotion, Math.min(1.0, score * intensityFactor));
        }
    }

    private EmotionalProfile analyzeEmotionalProfile(Map<String, Double> emotions) {
        EmotionalProfile profile = new EmotionalProfile();

        String dominantEmotion = emotions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");

        profile.setDominantEmotion(dominantEmotion);
        profile.setIntensity(emotions.get(dominantEmotion));
        profile.setEmotionalComplexity(calculateEmotionalComplexity(emotions));
        profile.setEmotionalShifts(detectEmotionalShifts(emotions));

        return profile;
    }

    private List<SymbolicMeaning> analyzeSymbolicMeanings(List<DreamSymbol> symbols) {
        return symbols.stream()
                .map(symbol -> new SymbolicMeaning(symbol.getName(), symbol.getMeaning().getInterpretation()))
                .collect(Collectors.toList());
    }

    private NarrativeStructure analyzeNarrativeStructure(String text) {
        NarrativeStructure structure = new NarrativeStructure();

        structure.setLengthCategory(analyzeTextLength(text));
        structure.setNarrativeFlow(detectNarrativeFlow(text));
        structure.setKeyEvents(extractKeyEvents(text));

        return structure;
    }

    private List<PsychologicalTheme> identifyPsychologicalThemes(String text, Map<String, Double> emotions, List<DreamSymbol> symbols) {
        List<PsychologicalTheme> themes = new ArrayList<>();

        // Analyze for common psychological themes
        if (emotions.getOrDefault("fear", 0.0) > 0.6) {
            themes.add(new PsychologicalTheme("Anxiety", "Processing fears and uncertainties"));
        }
        if (emotions.getOrDefault("joy", 0.0) > 0.6) {
            themes.add(new PsychologicalTheme("Contentment", "Positive emotional integration"));
        }
        if (symbols.stream().anyMatch(s -> s.getName().equals("water") && s.getConfidence() > 0.7)) {
            themes.add(new PsychologicalTheme("Emotional Processing", "Working through feelings and subconscious material"));
        }
        // Add remaining themes
        if (emotions.getOrDefault("anger", 0.0) > 0.5) {
            themes.add(new PsychologicalTheme("Conflict Resolution", "Processing anger or frustration"));
        }
        if (symbols.stream().anyMatch(s -> s.getName().equals("falling"))) {
            themes.add(new PsychologicalTheme("Control Issues", "Concerns about losing control or stability"));
        }
        if (symbols.stream().anyMatch(s -> s.getName().equals("house"))) {
            themes.add(new PsychologicalTheme("Self-Exploration", "Examining different aspects of personality"));
        }

        return themes;
    }

    private List<PersonalInsight> generatePersonalInsights(String text, Map<String, Double> emotions, List<DreamSymbol> symbols) {
        List<PersonalInsight> insights = new ArrayList<>();

        // Generate personalized insights based on analysis
        if (emotions.getOrDefault("confusion", 0.0) > 0.5) {
            insights.add(new PersonalInsight("Consider areas in your life where you feel uncertain or indecisive."));
        }

        if (symbols.stream().anyMatch(s -> s.getName().equals("flying"))) {
            insights.add(new PersonalInsight("The flying motif suggests a desire for freedom or transcendence in your waking life."));
        }

        // Add remaining insights
        if (emotions.getOrDefault("peace", 0.0) > 0.6) {
            insights.add(new PersonalInsight("Your dream reflects inner harmony. Consider what brings you peace in daily life."));
        }

        if (symbols.stream().anyMatch(s -> s.getName().equals("water"))) {
            insights.add(new PersonalInsight("Water symbols often relate to emotions. Reflect on your current emotional state."));
        }

        if (emotions.getOrDefault("anxiety", 0.0) > 0.5) {
            insights.add(new PersonalInsight("This dream may highlight areas where you feel pressured or overwhelmed."));
        }

        if (symbols.stream().anyMatch(s -> s.getName().equals("car"))) {
            insights.add(new PersonalInsight("Consider the direction your life is taking and whether you feel in control."));
        }

        return insights;
    }

    // Utility calculations
    private double calculateEmotionalComplexity(Map<String, Double> emotions) {
        long significantEmotions = emotions.values().stream()
                .filter(score -> score > 0.3)
                .count();
        return Math.min(1.0, significantEmotions / 3.0);
    }

    private boolean detectEmotionalShifts(Map<String, Double> emotions) {
        return emotions.values().stream()
                .filter(score -> score > 0.4)
                .count() > 2;
    }

    private String analyzeTextLength(String text) {
        int wordCount = text.split("\\s+").length;
        if (wordCount < 50) return "Brief";
        if (wordCount < 200) return "Moderate";
        return "Detailed";
    }

    private String detectNarrativeFlow(String text) {
        String[] sentences = text.split("[.!?]+");
        if (sentences.length < 3) return "Fragmented";
        if (sentences.length < 8) return "Structured";
        return "Complex";
    }

    private List<String> extractKeyEvents(String text) {
        List<String> events = new ArrayList<>();
        Pattern eventPattern = Pattern.compile("\\b(I|we|he|she|they)\\s+(was|were|ran|flew|fell|saw|met|found)\\s+[^.!?]*[.!?]");

        eventPattern.matcher(text).results()
                .limit(5)
                .forEach(match -> events.add(match.group()));

        return events;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // ========== LEXICON CREATION METHODS ==========

    private static Map<String, EmotionPattern> createEmotionLexicon() {
        Map<String, EmotionPattern> lexicon = new HashMap<>();

        lexicon.put("fear", new EmotionPattern()
                .withWords("scared", "afraid", "terrified", "frightened", "panic", "horror")
                .withPhrases("running away", "being chased", "heart pounding", "couldn't move")
                .withWeight(1.0)
                .withPhraseWeight(1.5));

        lexicon.put("joy", new EmotionPattern()
                .withWords("happy", "joy", "delighted", "ecstatic", "bliss", "elated")
                .withPhrases("laughing together", "felt wonderful", "overflowing with happiness")
                .withWeight(1.0)
                .withPhraseWeight(1.5));

        lexicon.put("anxiety", new EmotionPattern()
                .withWords("worried", "nervous", "anxious", "stressed", "tense", "uneasy")
                .withPhrases("late for", "couldn't find", "lost in", "forgot something")
                .withWeight(0.9)
                .withPhraseWeight(1.3));

        lexicon.put("sadness", new EmotionPattern()
                .withWords("sad", "depressed", "mournful", "grief", "sorrow", "melancholy")
                .withPhrases("crying alone", "felt empty", "everything was grey", "lost forever")
                .withWeight(1.0)
                .withPhraseWeight(1.4));

        lexicon.put("peace", new EmotionPattern()
                .withWords("calm", "peaceful", "serene", "tranquil", "content", "relaxed")
                .withPhrases("floating gently", "quiet stillness", "warm comfort", "soft light")
                .withWeight(0.8)
                .withPhraseWeight(1.2));

        lexicon.put("excitement", new EmotionPattern()
                .withWords("excited", "thrilled", "energetic", "enthusiastic", "eager", "animated")
                .withPhrases("heart racing", "couldn't wait", "bursting with energy", "adventure awaits")
                .withWeight(0.9)
                .withPhraseWeight(1.3));

        lexicon.put("confusion", new EmotionPattern()
                .withWords("confused", "disoriented", "bewildered", "perplexed", "puzzled", "lost")
                .withPhrases("didn't make sense", "everything changed", "couldn't understand", "maze-like")
                .withWeight(0.8)
                .withPhraseWeight(1.2));

        lexicon.put("anger", new EmotionPattern()
                .withWords("angry", "furious", "enraged", "irritated", "frustrated", "outraged")
                .withPhrases("screaming loudly", "red with anger", "tearing things", "stormed out")
                .withWeight(1.0)
                .withPhraseWeight(1.5));

        // Add remaining emotions
        lexicon.put("surprise", new EmotionPattern()
                .withWords("surprised", "shocked", "astonished", "amazed", "startled")
                .withPhrases("couldn't believe", "suddenly appeared", "unexpected turn")
                .withWeight(0.7)
                .withPhraseWeight(1.1));

        lexicon.put("love", new EmotionPattern()
                .withWords("love", "affection", "caring", "devotion", "passion")
                .withPhrases("heart filled with", "embraced warmly", "deep connection")
                .withWeight(0.9)
                .withPhraseWeight(1.4));

        return lexicon;
    }

    private static Map<String, SymbolMeaning> createSymbolDatabase() {
        Map<String, SymbolMeaning> database = new HashMap<>();

        database.put("water", new SymbolMeaning()
                .withInterpretation("Represents emotions, subconscious mind, purification, and life transitions")
                .withContextClues("ocean", "river", "rain", "flood", "swimming", "drowning")
                .withMetaphors("emotional flow", "deep feelings", "cleansing tears"));

        database.put("flying", new SymbolMeaning()
                .withInterpretation("Symbolizes freedom, ambition, transcendence, and desire to escape limitations")
                .withContextClues("soaring", "floating", "wings", "sky", "clouds")
                .withMetaphors("reaching new heights", "free spirit", "unlimited potential"));

        database.put("falling", new SymbolMeaning()
                .withInterpretation("Indicates loss of control, insecurity, fear of failure, or letting go")
                .withContextClues("plummeting", "descending", "cliff", "height", "drop")
                .withMetaphors("losing grip", "sinking feeling", "downward spiral"));

        database.put("house", new SymbolMeaning()
                .withInterpretation("Represents the self, mind, different aspects of personality, or security")
                .withContextClues("rooms", "doors", "windows", "basement", "attic")
                .withMetaphors("inner self", "mental spaces", "personal boundaries"));

        database.put("car", new SymbolMeaning()
                .withInterpretation("Symbolizes life direction, personal control, journey, or motivation")
                .withContextClues("driving", "road", "steering wheel", "engine", "passenger")
                .withMetaphors("life path", "personal drive", "direction in life"));

        // Add remaining symbols
        database.put("death", new SymbolMeaning()
                .withInterpretation("Represents transformation, endings, rebirth, or major life changes")
                .withContextClues("died", "ghost", "cemetery", "funeral", "afterlife")
                .withMetaphors("end of era", "spiritual transition", "letting go"));

        database.put("school", new SymbolMeaning()
                .withInterpretation("Symbolizes learning, personal growth, evaluation, or past experiences")
                .withContextClues("classroom", "teacher", "exam", "homework", "graduation")
                .withMetaphors("life lessons", "personal development", "self-evaluation"));

        database.put("animal", new SymbolMeaning()
                .withInterpretation("Represents instincts, primal nature, or specific animal traits")
                .withContextClues("wild", "pet", "hunting", "running", "growling")
                .withMetaphors("primal instincts", "natural behavior", "inner nature"));

        database.put("fire", new SymbolMeaning()
                .withInterpretation("Symbolizes passion, transformation, destruction, or purification")
                .withContextClues("flames", "burning", "heat", "light", "smoke")
                .withMetaphors("burning desire", "transformative energy", "cleansing fire"));

        database.put("money", new SymbolMeaning()
                .withInterpretation("Represents self-worth, value, resources, or emotional currency")
                .withContextClues("cash", "rich", "poor", "coins", "wealth")
                .withMetaphors("self-value", "emotional resources", "personal worth"));

        database.put("bridge", new SymbolMeaning()
                .withInterpretation("Symbolizes transitions, connections, decisions, or life changes")
                .withContextClues("crossing", "river", "gap", "connection", "path")
                .withMetaphors("life transition", "making connections", "bridging gaps"));

        database.put("tree", new SymbolMeaning()
                .withInterpretation("Represents growth, stability, family roots, or personal development")
                .withContextClues("forest", "roots", "branches", "leaves", "growing")
                .withMetaphors("personal growth", "family roots", "life stability"));

        database.put("mirror", new SymbolMeaning()
                .withInterpretation("Symbolizes self-reflection, truth, identity, or hidden aspects")
                .withContextClues("reflection", "glass", "image", "looking", "double")
                .withMetaphors("self-examination", "facing truth", "hidden self"));

        return database;
    }

    private static Map<String, Pattern> createLinguisticPatterns() {
        Map<String, Pattern> patterns = new HashMap<>();

        patterns.put("fear", Pattern.compile("\\b(scared|afraid|terrified|frightening|horror|panic|nightmare|monster|danger|threat|chase|dark|shadow|hide)\\b"));
        patterns.put("joy", Pattern.compile("\\b(happy|joy|delight|excited|wonderful|amazing|beautiful|love|smile|laugh|celebration|peaceful|flying|light|bright)\\b"));
        patterns.put("anxiety", Pattern.compile("\\b(worried|anxious|nervous|stress|tension|overwhelmed|confused|lost|trapped|hurried|late|exam|falling|searching)\\b"));
        patterns.put("sadness", Pattern.compile("\\b(sad|crying|tears|lonely|empty|dark|gloomy|depressed|grief|loss|death|grey|rain|alone|abandoned)\\b"));
        patterns.put("peace", Pattern.compile("\\b(calm|peaceful|serene|quiet|gentle|soft|warm|comfort|safe|relaxed|tranquil|still|floating|gentle)\\b"));
        patterns.put("excitement", Pattern.compile("\\b(thrilled|energetic|adventure|flying|fast|rushing|bright|intense|powerful|exhilarating|racing|bursting)\\b"));
        patterns.put("confusion", Pattern.compile("\\b(confused|strange|weird|bizarre|unclear|foggy|mixed|chaotic|disoriented|maze|lost|uncertain|puzzled)\\b"));
        patterns.put("anger", Pattern.compile("\\b(angry|furious|enraged|irritated|frustrated|outraged|screaming|yelling|fighting|red|hot|storming)\\b"));

        // Add remaining patterns
        patterns.put("surprise", Pattern.compile("\\b(surprised|shocked|astonished|amazed|startled|unexpected|suddenly)\\b"));
        patterns.put("love", Pattern.compile("\\b(love|affection|caring|devotion|passion|romance|heart|embrace|kiss|hug)\\b"));

        return patterns;
    }

    // ========== SUPPORTING DATA CLASSES ==========

    public static class EmotionPattern {
        private Set<String> words = new HashSet<>();
        private Set<String> phrases = new HashSet<>();
        private double weight = 1.0;
        private double phraseWeight = 1.5;

        public EmotionPattern withWords(String... words) {
            this.words.addAll(Arrays.asList(words));
            return this;
        }

        public EmotionPattern withPhrases(String... phrases) {
            this.phrases.addAll(Arrays.asList(phrases));
            return this;
        }

        public EmotionPattern withWeight(double weight) {
            this.weight = weight;
            return this;
        }

        public EmotionPattern withPhraseWeight(double phraseWeight) {
            this.phraseWeight = phraseWeight;
            return this;
        }

        public boolean matches(String word) {
            return words.contains(word);
        }

        public Set<String> getWords() { return words; }
        public Set<String> getPhrases() { return phrases; }
        public double getWeight() { return weight; }
        public double getPhraseWeight() { return phraseWeight; }
    }

    public static class SymbolMeaning {
        private String interpretation;
        private Set<String> contextClues = new HashSet<>();
        private Set<String> metaphors = new HashSet<>();

        public SymbolMeaning withInterpretation(String interpretation) {
            this.interpretation = interpretation;
            return this;
        }

        public SymbolMeaning withContextClues(String... clues) {
            this.contextClues.addAll(Arrays.asList(clues));
            return this;
        }

        public SymbolMeaning withMetaphors(String... metaphors) {
            this.metaphors.addAll(Arrays.asList(metaphors));
            return this;
        }

        public String getInterpretation() { return interpretation; }
        public Set<String> getContextClues() { return contextClues; }
        public Set<String> getMetaphors() { return metaphors; }
    }

    public static class DreamSymbol {
        private String name;
        private SymbolMeaning meaning;
        private double confidence;

        public DreamSymbol(String name, SymbolMeaning meaning) {
            this.name = name;
            this.meaning = meaning;
        }

        public String getName() { return name; }
        public SymbolMeaning getMeaning() { return meaning; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    public static class DreamInterpretation {
        private EmotionalProfile emotionalProfile;
        private List<SymbolicMeaning> symbolicMeanings;
        private NarrativeStructure narrativeStructure;
        private List<PsychologicalTheme> psychologicalThemes;
        private List<PersonalInsight> personalInsights;

        public EmotionalProfile getEmotionalProfile() { return emotionalProfile; }
        public void setEmotionalProfile(EmotionalProfile emotionalProfile) { this.emotionalProfile = emotionalProfile; }
        public List<SymbolicMeaning> getSymbolicMeanings() { return symbolicMeanings; }
        public void setSymbolicMeanings(List<SymbolicMeaning> symbolicMeanings) { this.symbolicMeanings = symbolicMeanings; }
        public NarrativeStructure getNarrativeStructure() { return narrativeStructure; }
        public void setNarrativeStructure(NarrativeStructure narrativeStructure) { this.narrativeStructure = narrativeStructure; }
        public List<PsychologicalTheme> getPsychologicalThemes() { return psychologicalThemes; }
        public void setPsychologicalThemes(List<PsychologicalTheme> psychologicalThemes) { this.psychologicalThemes = psychologicalThemes; }
        public List<PersonalInsight> getPersonalInsights() { return personalInsights; }
        public void setPersonalInsights(List<PersonalInsight> personalInsights) { this.personalInsights = personalInsights; }
    }

    public static class EmotionalProfile {
        private String dominantEmotion;
        private double intensity;
        private double emotionalComplexity;
        private boolean emotionalShifts;

        public String getDominantEmotion() { return dominantEmotion; }
        public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
        public double getIntensity() { return intensity; }
        public void setIntensity(double intensity) { this.intensity = intensity; }
        public double getEmotionalComplexity() { return emotionalComplexity; }
        public void setEmotionalComplexity(double emotionalComplexity) { this.emotionalComplexity = emotionalComplexity; }
        public boolean hasEmotionalShifts() { return emotionalShifts; }
        public void setEmotionalShifts(boolean emotionalShifts) { this.emotionalShifts = emotionalShifts; }
    }

    public static class SymbolicMeaning {
        private String symbol;
        private String meaning;

        public SymbolicMeaning(String symbol, String meaning) {
            this.symbol = symbol;
            this.meaning = meaning;
        }

        public String getSymbol() { return symbol; }
        public String getMeaning() { return meaning; }
    }

    public static class NarrativeStructure {
        private String lengthCategory;
        private String narrativeFlow;
        private List<String> keyEvents;

        public String getLengthCategory() { return lengthCategory; }
        public void setLengthCategory(String lengthCategory) { this.lengthCategory = lengthCategory; }
        public String getNarrativeFlow() { return narrativeFlow; }
        public void setNarrativeFlow(String narrativeFlow) { this.narrativeFlow = narrativeFlow; }
        public List<String> getKeyEvents() { return keyEvents; }
        public void setKeyEvents(List<String> keyEvents) { this.keyEvents = keyEvents; }
    }

    public static class PsychologicalTheme {
        private String name;
        private String description;

        public PsychologicalTheme(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    public static class PersonalInsight {
        private String insight;

        public PersonalInsight(String insight) {
            this.insight = insight;
        }

        public String getInsight() { return insight; }
    }
}