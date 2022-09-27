FROM python:2.7

ENV SRV localhost:8080

ADD . .
RUN pip install -r requirments.txt

ENTRYPOINT [ "/usr/local/bin/python", "e2e_test.py" ]
CMD ["$SRV"]