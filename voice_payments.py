import pyttsx3
import speech_recognition as sr
from nltk.stem import PorterStemmer
from nltk.tokenize import word_tokenize

def speak_text(text):
    engine = pyttsx3.init()
    engine.say(text)
    engine.runAndWait()

def listen_for_confirmation():
    recognizer = sr.Recognizer()
    with sr.Microphone() as source:
        recognizer.adjust_for_ambient_noise(source, duration=1)
        print("Listening...")
        audio = recognizer.listen(source)
        try:
            response = recognizer.recognize_google(audio)
            print(f"You said: {response}")
            return response.lower()
        except sr.UnknownValueError:
            print("Sorry, I did not understand that.")
            return None
        except sr.RequestError:
            print("Sorry, my speech service is down.")
            return None

def get_stemmed_positive_words():
    positive_words = [
        "yes", "yep", "yeah", "affirmative", "sure", "right", "correct",
        "confirm", "confirmed", "ok", "okay", "accept", "accepted", 
        "authorize", "authorized", "approve", "approved", "payment", 
        "processed", "completed", "done", "success"
    ]
    
    stemmer = PorterStemmer()
    stemmed_positive_words = [stemmer.stem(word) for word in positive_words]
    return stemmed_positive_words

def main():
    stemmed_positive_words = get_stemmed_positive_words()

    try:
        while True:
            input_text = input("Enter the text to be spoken: ")
            speak_text(input_text)
            
            question = "Do you confirm the above statement?"

            for attempt in range(3):
                speak_text(question)
                response = listen_for_confirmation()
                if response:
                    stemmed_response_words = [PorterStemmer().stem(word) for word in word_tokenize(response)]
                    if any(word in stemmed_response_words for word in stemmed_positive_words):
                        speak_text("Thank you for your confirmation.")
                        break
                    else:
                        print("No positive confirmation detected. Retrying...")
                else:
                    print("No valid response detected. Retrying...")

            else:
                speak_text("Sorry, no confirmation received. Please try again later.")
                
    except KeyboardInterrupt:
        print("\nProgram terminated by user.")

if __name__ == "__main__":
    import nltk
    nltk.download('punkt')  # Download tokenizer data
    main()
