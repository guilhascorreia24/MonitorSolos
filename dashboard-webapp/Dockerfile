# Use lightweight Node.js 20 image
FROM node:20-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source
COPY . .

# Build step (optional for dev, but good for verification)
# RUN npm run build

# Expose port (Next.js default is 3000, we map it to 3100 in compose)
EXPOSE 3000

# Start in dev mode to allow easy exploration
CMD ["npm", "run", "dev"]
