curl -X POST "http://localhost:9010/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "userAccount": "wwei",
    "userPassword": "12345678"
  }' \
  -c cookies.txt

curl -G "http://localhost:9010/api/app/chat/gen/code" \
  --data-urlencode "appId=360639866459975680" \
  --data-urlencode "message= Make a blog of WuWei" \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  -b cookies.txt \
  --no-buffer

read -n 1

