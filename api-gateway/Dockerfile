# STAGE 1: Build
# Utiliza uma imagem com o JDK completo para compilar a aplicação
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copia primeiro os arquivos de dependência para aproveitar o cache do Docker
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixa as dependências do projeto
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copia o código-fonte da aplicação
COPY src ./src

# Compila e empacota a aplicação, pulando os testes
RUN ./mvnw package -DskipTests

# STAGE 2: Run
# Utiliza uma imagem JRE mínima para a execução, resultando numa imagem final menor
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia o JAR compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Comando para executar a aplicação quando o container iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
