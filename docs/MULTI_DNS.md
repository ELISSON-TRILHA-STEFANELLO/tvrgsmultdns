# Login rápido (multi-DNS)

A tela principal do app agora pede só **usuário** e **senha**. Por trás, o app tenta
automaticamente uma lista de servidores ("DNS") que você controla, na ordem que você definir,
até um aceitar o login — o cliente nunca precisa saber o endereço do servidor.

## O que você precisa fazer

Hospede um arquivo JSON público (HTTP ou HTTPS) com a lista de servidores, no formato abaixo, e
aponte a URL dele em `MultiDnsRepositoryImpl.MULTI_DNS_CONFIG_URL`
(`data/src/main/java/com/tvrgs/data/repository/MultiDnsRepositoryImpl.kt`).

Por padrão o app está apontando para `https://tvrgs.com/app/multidns.json` — troque se for usar
outro endereço.

### Formato do JSON

```json
{
  "hosts": [
    { "name": "Servidor 1", "url": "http://dns1.seudominio.com:8080", "priority": 1 },
    { "name": "Servidor 2", "url": "http://dns2.seudominio.com:8080", "priority": 2 },
    { "name": "Servidor 3", "url": "http://dns3.seudominio.com:8080", "priority": 3 }
  ]
}
```

- `url`: endereço base do Xtream Codes (sem `/player_api.php`, sem barra no final).
- `priority`: ordem de tentativa — menor número é tentado primeiro.
- `name`: só para exibição/depuração (aparece no indicador "Conectando em…" durante o login).

## Como funciona

1. O app baixa esse JSON e guarda em cache local (6 horas) — se o site cair, ele continua
   funcionando com a última lista salva.
2. Ao digitar usuário/senha, o app tenta cada host da lista em ordem até um autenticar.
3. Quando um host funciona, o app lembra qual foi (por dispositivo) e tenta ele primeiro nas
   próximas vezes — sem precisar testar todos de novo.
4. Você pode adicionar, remover ou reordenar servidores só editando o JSON no seu site — não
   precisa gerar um novo APK para isso.

## Configuração avançada continua disponível

Quem precisa configurar Stalker Portal, Jellyfin, M3U manual, ou adicionar um segundo provedor
Xtream com endereço específico, encontra essas opções em **Configurações → Adicionar provedor**
— a tela inicial só mostra o login simples.
