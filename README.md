## Autorização de Pagamentos

Criei esse projeto utilizando Scala, porém nunca tinha utilizado a linguagem nem as ferramentas do seu ecosistema. Provavelmente deve ter alguns antipatterns ou más praticas no codigo. Porém decidi fazer em scala também para mostrar a capacidade de aprendizado de ferramentas novas.

### Como executar

Criei o Projeto utilizando sbt. então para escecutar basta rodar `sbt run` e usar também o docker compose para subir o banco de dados.

### Transações simultaneas

O problema de concorrencie em pensei em duas coisa, que são básicas, porém funcionam. Primeiro a modelagem do banco eu separei o saldo de cada categoria em uma linha em uma tabela expecifica, por que dessa forma consigo usar um lock do banco de dados em um saldo sem afetar os outros, tornando assim a concorrencia entre duas transações entre saldos diferentes praticamente sem efeitos de timeout (obviamente depende da escala).

```
+-----------------------+                      +------------------------+                                         
|         ACCOUNT       |                      |     ACCOUNT BALANCE    |                                         
|-----------------------|         1: N         |------------------------|                                         
| id: UUID              |--------------------- | id: UUID               |                                         
| account: VARCHAR      |                      | acount_id: UUID FK     |                                         
| total_balance: DOUBLE |                      | category: VARCHAR      |                                         
+-----------------------+                      | balance: DOUBLE        |                                         
                                               +------------------------+
```
Agora para concorrencia entre transações na mesma categoria, a solução para o problema de concorrencia é primeiro, usar transações em todas as operações de escrita no banco, como a modelagem do banco ajuda, poderiamos usar uma mescla de `RowShareLock` para a leitura dos registros caso mais de uma transação chegasse para a mesma categoria e o `RowExclusiveLock` para escrever nessa linha.
Outra coisa que fiz foi salvar um valor computado de todos os saldos disponiveis na tabela account, pois assim eu consigo verificar se a transação porerá ser realizada antes mesmo de buscar os saldos de categorias especificas.