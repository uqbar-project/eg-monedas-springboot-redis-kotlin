FROM node:22

# Activar Corepack (viene con Node)
RUN corepack enable

WORKDIR /usr/src/app

# Copiás solo los manifests
COPY datosInicialesNode/package.json datosInicialesNode/pnpm-lock.yaml ./

# Instalar dependencias con pnpm
RUN pnpm install --frozen-lockfile

# Copiás el resto
COPY . .

EXPOSE 8080

CMD ["node", "datosInicialesNode/cargaDatosInicial.js"]