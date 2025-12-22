provider "aws" {
  region = "us-east-1"
}

resource "aws_key_pair" "memex_auth" {
  key_name   = "memex-ssh-key"
  public_key = file("~/.ssh/memex_key.pub")
}

# 1. AUTO-DETECT the latest Ubuntu AMI
# (This prevents "Invalid AMI" errors by asking AWS for the current ID)
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical (Official Ubuntu Publisher)

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# 2. Security Group (Firewall)
resource "aws_security_group" "memex_sg" {
  name        = "memex-security-group"
  description = "Allow Web and SSH traffic"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# 3. Create the EC2 Instance
resource "aws_instance" "memex_server" {
  # Use the ID we found automatically in step 1
  ami           = data.aws_ami.ubuntu.id
  
  # Switch to t3.micro (Newer Free Tier standard)
  instance_type = "t3.micro"

  key_name      = aws_key_pair.memex_auth.key_name
  
  # Attach Firewall
  vpc_security_group_ids = [aws_security_group.memex_sg.id]

  # Install Docker on startup
  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update
              sudo apt-get install -y docker.io
              sudo systemctl start docker
              sudo systemctl enable docker
              sudo usermod -aG docker ubuntu
              EOF

  tags = {
    Name = "Memex-Production-Server"
  }
}

output "server_ip" {
  value = aws_instance.memex_server.public_ip
}