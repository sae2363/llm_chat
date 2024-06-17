from typing import Dict
from flask import Flask, jsonify, request,json
import LLM, subprocess

app = Flask(__name__)
history=[]
@app.route("/")
def hello_world():
    return "<p>Hello, World!</p>"

#incoming data format {token:int,prompt:str,basePrompt:str}
#change from old, token is now index of array (unused)
@app.route('/SendMessage', methods=['POST'])
def post_message():
    global history  
    data = request.json
    param1 = data['token']
    prompt = data['prompt']
    base_prompt = data.get('basePrompt', "")

    if not base_prompt:
        base_prompt = "You are a helpful chatbot and will answer my questions"

    if not history:
        history.append({"role": "system", "content": base_prompt})

    info = {
        'prompt': prompt,
        'basePrompt': base_prompt
    }

    print(history)
    history = LLM.sendMessage2(param1, info, history)
    print(history)
    return "<p>Data received</p>", 200

#incoming data format {token:int,prompt:str,basePrompt:str,image:str(base64)}
@app.route('/SendImage', methods=['POST'])
def post_image():
    global history  
    data = request.json
    param1 = data['token']
    prompt = data['prompt']
    base_prompt = data.get('basePrompt', "")
    image=data['iamge']
    if not base_prompt:
        base_prompt = "You are a helpful chatbot and will answer my questions"

    if not history:
        history.append({"role": "system", "content": base_prompt})

    info = {
        'prompt': prompt,
        'basePrompt': base_prompt
    }

    print(history)
    history = LLM.sendMessage2(param1, info, history,image)
    print(history)
    return "<p>Data received</p>", 200

@app.route('/ClearData', methods=['GET'])
def clear_data():
    global history 
    history.clear()
    print("clear")
    return "<p>Data cleared successfully</p>", 200


@app.route('/getdata',methods=['GET'])
def get_data():
    global history
    param1 =int(request.args.get('token'))
    print(history)
    if(len(history)!=0 and history[-1]['role']=="assistant"):
        try:
            return jsonify(history[-1]['content']),200
        except:
            return jsonify({"error": "Parameter not found"}), 404
    return jsonify({"error": "history is empty"}), 404

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001)


#to run flask --app C:\\Users\\Sae\\Documents\\GitHub\\test_server\\server\\api.py run -h 127.0.0.1 -p 5001
#to run tunnel cloudflared tunnel --url http://127.0.0.1:5001
