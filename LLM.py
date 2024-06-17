import json
import requests
import sseclient  # pip install sseclient-py
import base64
from ollama import Client

#url="http://127.0.0.1:5000/"
#urlCom = "http://127.0.0.1:5000/v1/completions"
#urlChat = "http://127.0.0.1:5000/v1/chat/completions"
url="http://192.168.1.2:5000/"
urlCom = url+"v1/completions"
urlChat = url+"v1/chat/completions"

headers = {
    "Content-Type": "application/json"
}

data = {
    "prompt": "who are you",#"This is a cake recipe:\n\n1.",
    "max_tokens": 200,
    "temperature": 1,
    "top_p": 0.9,
    "seed": 10,
    "stream": True,
}

def getModelInfo():
    return requests.get(url+"api/tags").json()

def getModelNames():
    return [model["name"] for model in getModelInfo()["models"]]

def getLoadedModel():
    return requests.get(url+"api/ps").json()

#not needed in ollama as model is specified in call
def loadModel(name):
    model={
    "model_name": name,
    "args": {},
    "settings": {}
    }
    requests.post(url+"v1/internal/model/load",json=model)
#open ai api style
def sendMessage1(token,info,history):
    basePrompt="You are a helpful assistant that will answer my prompt "
    if(info['basePrompt']!=""):
        basePrompt=info['basePrompt']
        
    history.append({"role": "user", "content": info['prompt']})
    data = {
        "mode": "chat-instruct",
        "model":"Hermes-2-Pro-Llama-3-8B-Q6_K",
        "character": "Assistant",
        "messages": history,
    }
    response = requests.post(urlChat, headers=headers, json=data, verify=False)
    print(response.json())
    assistant_message = response.json()['choices'][0]['message']['content']
    history.append({"role": "assistant", "content": assistant_message})
    return history
#ollama version
def sendMessage2(token,info,history):
    basePrompt="You are a helpful assistant that will answer my prompt"
    if(info['basePrompt']!=""):
        basePrompt=info['basePrompt']
        
    history.append({"role": "user", "content": info['prompt']})
    data = {
        "model":"Hermes-2-Pro-Llama-3-8B-Q6_K",
        "messages": history,
    }
    response = requests.post(urlChat, headers=headers, json=data, verify=False)
    print(response.json())
    assistant_message = response.json()['choices'][0]['message']['content']
    history.append({"role": "assistant", "content": assistant_message})
    return history

def sendMessageImage(token,info,history,image):
    client = Client(host='http://192.168.1.2:5000/')
    basePrompt="You are a helpful assistant that will answer my prompt"
    if(info['basePrompt']!=""):
        basePrompt=info['basePrompt']
    images=[image]
    history.append({"role": "user", "content": info['prompt'],"images":images})
    #print(history)
    data = {
        "model":"llava",
        "messages": history,
    }
    response = client.chat(model="llava",messages=history,stream=False)
    #response = requests.post(urlChat, headers=headers, json=data, verify=False)
    assistant_message = response['message']['content']
    history.append({"role": "assistant", "content": assistant_message,})
    return history



"""dataModel = requests.get(url+"v1/internal/model/list").json()
model_names = dataModel['model_names']
for model_name in model_names:
    print(model_name)
modell={
  "model_name": model_names[0],
  "args": {},
  "settings": {}
}
requests.post(url+"v1/internal/model/load",json=modell)

history = []

while True:
    user_message = input("> ")
    history.append({"role": "user/human", "content": user_message})
    data = {
        "mode": "chat",
        "character": "example",
        "messages": history
    }

    response = requests.post(urlChat, headers=headers, json=data, verify=False)
    #print(response.json()['choices'])
    assistant_message = response.json()['choices'][0]['message']['content']
    history.append({"role": "assistant", "content": assistant_message})
    print(assistant_message)

stream_response = requests.post(urlCom, headers=headers, json=data, verify=False, stream=True)
client = sseclient.SSEClient(stream_response)

print(data["prompt"], end='')
for event in client.events():
    payload = json.loads(event.data)
    print(payload['choices'][0]['text'], end='')

print()"""