FROM node:12

RUN npm install -g @angular/cli

VOLUME /var/xdcs
WORKDIR /var/xdcs/xdcs-frontend

EXPOSE 8080

CMD npm install && npm run generate && ng build && \
  ng serve --host 0.0.0.0 --port 8080 --proxy-config proxy.conf.json
