1. 准备 Dockerfile

首先，你需要创建一个 Dockerfile 来定义你的 Docker 镜像。以下是一个简单的 Dockerfile 示例：
Dockerfile
复制

# 使用官方 Python 镜像作为基础镜像
FROM python:3.9-slim

# 升级 pip
RUN pip install --upgrade pip

# 设置工作目录
WORKDIR /app

# 复制当前目录下的所有文件到容器的 /app 目录
COPY . /app

# 安装所需的 Python 依赖
RUN pip install --no-cache-dir -r requirements.txt

# 暴露端口（如果需要）
EXPOSE 5000

# 设置环境变量（如果需要）
ENV MODEL_NAME=deepseek-1.5b

# 运行命令
CMD ["python", "app.py"]

2. 准备 requirements.txt

在 requirements.txt 文件中列出所有需要的 Python 依赖包。例如：
plaintext
复制

torch
transformers
flask

3. 准备 app.py

创建一个 app.py 文件来加载模型并启动服务。以下是一个简单的示例：
python
复制

from transformers import AutoModelForCausalLM, AutoTokenizer
import torch
from flask import Flask, request, jsonify

app = Flask(__name__)

# 加载模型和分词器
model_name = "deepseek-ai/deepseek-1.5b"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForCausalLM.from_pretrained(model_name)

@app.route('/generate', methods=['POST'])
def generate():
    data = request.json
    input_text = data.get('input_text', '')
    
    # 生成文本
    inputs = tokenizer(input_text, return_tensors="pt")
    outputs = model.generate(**inputs, max_length=50)
    generated_text = tokenizer.decode(outputs[0], skip_special_tokens=True)
    
    return jsonify({"generated_text": generated_text})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

4. 构建 Docker 镜像

在包含 Dockerfile、requirements.txt 和 app.py 的目录中，运行以下命令来构建 Docker 镜像：
bash
复制

docker build -t deepseek-1.5b .

5. 运行 Docker 容器

构建完成后，你可以使用以下命令来运行 Docker 容器：
bash
复制

docker run -p 5000:5000 deepseek-1.5b

6. 测试服务

你可以使用 curl 或 Postman 来测试服务。例如：
bash
复制

curl -X POST http://localhost:5000/generate -H "Content-Type: application/json" -d '{"input_text": "Hello, how are you?"}'

7. 部署到生产环境

如果你需要将服务部署到生产环境，可以考虑使用 Kubernetes、Docker Compose 或其他容器编排工具来管理容器。
注意事项

    由于 DeepSeek 1.5B 模型较大，确保你的 Docker 容器有足够的内存和计算资源。

    如果你在 GPU 上运行模型，可以使用 nvidia-docker 来加速推理。

通过以上步骤，你应该能够在 Docker 中成功部署 DeepSeek 1.5B 模型。